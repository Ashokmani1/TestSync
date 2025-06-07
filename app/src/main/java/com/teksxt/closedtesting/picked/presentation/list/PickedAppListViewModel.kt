package com.teksxt.closedtesting.picked.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.picked.domain.model.PickedApp
import com.teksxt.closedtesting.picked.domain.repo.PickedAppRepository
import com.teksxt.closedtesting.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickedAppListViewModel @Inject constructor(
    private val pickedAppRepository: PickedAppRepository,
    private val appRepository: AppRepository,
    private val requestRepository: RequestRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PickedAppListState())
    val state: StateFlow<PickedAppListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(PickedAppFilter.ALL)
    val selectedFilter = _selectedFilter.asStateFlow()

    // Cache for app details
    private val appDetailsCache = mutableMapOf<String, App>()

    // Original unfiltered data
    private var allPickedApps = listOf<PickedAppWithDetails>()

    private val requestIdCache = mutableMapOf<String, String>()

    init {
        loadPickedApps()

        // Filter whenever search query or filter changes
        combine(searchQuery, selectedFilter) { query, filter ->
            filterPickedApps(query, filter)
        }.launchIn(viewModelScope)
    }

    // Fetch testing status for a picked app
    private fun fetchTesterDayStatus(pickedApp: PickedApp) {
        viewModelScope.launch {
            val appId = pickedApp.appId
            val userId = pickedApp.userId
            val dayNumber = pickedApp.currentTestDay

            // First get the request ID for this app
            if (!requestIdCache.containsKey(appId)) {
                requestRepository.getRequestByAppID(appId).onSuccess { request ->
                    request?.let {
                        requestIdCache[appId] = it.id
                    }
                }
            }

            // If we have the request ID, fetch the tester day status
            val requestId = requestIdCache[appId] ?: return@launch

            requestRepository.getTesterDayStatus(requestId, userId, dayNumber).onSuccess { status ->
                // Find the app in our list and update its status
                val updatedList = allPickedApps.map { app ->
                    if (app.id == pickedApp.id) {
                        app.copy(testingStatus = status)
                    } else {
                        app
                    }
                }

                allPickedApps = updatedList
                filterPickedApps(_searchQuery.value, _selectedFilter.value)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: PickedAppFilter) {
        _selectedFilter.value = filter
    }

    fun loadPickedApps() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            pickedAppRepository.getUserPickedAppsFlow().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val pickedApps = result.data ?: emptyList()

                        // Create combined data objects with placeholders for app details
                        val combinedData = pickedApps.map { pickedApp ->
                            PickedAppWithDetails(
                                pickedApp = pickedApp,
                                app = appDetailsCache[pickedApp.appId]
                            )
                        }

                        // Store the unfiltered list
                        allPickedApps = combinedData

                        // Apply current filters
                        filterPickedApps(_searchQuery.value, _selectedFilter.value)

                        _state.value = _state.value.copy(isLoading = false)

                        // Fetch app details for each picked app
                        fetchAppDetails(pickedApps)

                        pickedApps.forEach { fetchTesterDayStatus(it) }
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private fun fetchAppDetails(pickedApps: List<PickedApp>) {
        viewModelScope.launch {
            // Get unique app IDs that we don't have in cache yet
            val appIdsToFetch = pickedApps
                .map { it.appId }
                .distinct()
                .filter { !appDetailsCache.containsKey(it) }

            // Fetch each app's details
            appIdsToFetch.forEach { appId ->
                appRepository.getAppById(appId).onSuccess { app ->
                    app?.let {
                        // Update the cache
                        appDetailsCache[appId] = it

                        // Update the state with the new app details
                        updateStateWithAppDetails()
                    }
                }
            }
        }
    }

    private fun updateStateWithAppDetails() {
        // Update all picked apps with app details
        allPickedApps = allPickedApps.map { combined ->
            combined.copy(app = appDetailsCache[combined.appId])
        }

        // Re-apply current filter with updated data
        filterPickedApps(_searchQuery.value, _selectedFilter.value)
    }

    private fun filterPickedApps(query: String, filter: PickedAppFilter) {
        val filteredApps = allPickedApps.filter { app ->
            // First apply search query filter
            val matchesQuery = query.isEmpty() ||
                    app.name.contains(query, ignoreCase = true) ||
                    app.description.contains(query, ignoreCase = true)

            // Then apply status/category filter
            val matchesFilter = when (filter) {
                PickedAppFilter.ALL -> true
                PickedAppFilter.ACTIVE -> app.status == "ACTIVE"
                PickedAppFilter.COMPLETED -> app.status == "COMPLETED"
                PickedAppFilter.PINNED -> app.isPinned
            }

            matchesQuery && matchesFilter
        }

        _state.value = _state.value.copy(pickedApps = filteredApps)
    }

    fun togglePinnedStatus(pickedAppId: String) {
        viewModelScope.launch {
            val currentPicked = allPickedApps.find { it.id == pickedAppId } ?: return@launch
            pickedAppRepository.togglePickedAppPin(pickedAppId)
        }
    }
}

enum class PickedAppFilter(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    PINNED("Pinned")
}