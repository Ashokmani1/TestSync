package com.teksxt.closedtesting.myrequest.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.core.util.ProgressUtils
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class MyRequestViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    // Create a mutable state flow to hold all requests
    private val _allRequests = MutableStateFlow<List<Request>>(emptyList())

    // Create a mutable state flow to hold filtered requests
    private val _filteredRequests = MutableStateFlow<List<Request>>(emptyList())
    val filteredRequests: StateFlow<List<Request>> = _filteredRequests.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Filter state
    private val _selectedFilter = MutableStateFlow(RequestFilter.ACTIVE)
    val selectedFilter = _selectedFilter.asStateFlow()

    // Store app details for each request
    private val _appDetails = MutableStateFlow<Map<String, App>>(emptyMap())
    val appDetails: StateFlow<Map<String, App>> = _appDetails.asStateFlow()

    private val _requestProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val requestProgress: StateFlow<Map<String, Float>> = _requestProgress.asStateFlow()


    init {
        loadRequests()
        refreshRequests()

        // Set up search query debouncing like in ExploreViewModel
        searchQuery
            .debounce(300)
            .onEach { filterRequests() }
            .launchIn(viewModelScope)

        // Monitor changes to all requests and update progress
        _allRequests
            .onEach { loadProgressData() }
            .launchIn(viewModelScope)
    }

    private fun loadRequests() {
        viewModelScope.launch {
            try {
                // Properly collect the Flow inside a coroutine
                requestRepository.getUserRequestsFlow().collect { requestsList ->
                    _allRequests.value = requestsList.data ?: emptyList()
                    filterRequests()
                    loadAppDetails(requestsList.data ?: emptyList())
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to load requests")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: RequestFilter) {
        _selectedFilter.value = filter
        filterRequests()
    }

    private fun filterRequests() {
        val query = _searchQuery.value
        val filter = _selectedFilter.value
        val allRequests = _allRequests.value

        val filtered = allRequests.filter { request ->
            // Apply search query filter
            (query.isEmpty() ||
                    request.title.contains(query, ignoreCase = true) ||
                    request.description?.contains(query, ignoreCase = true) == true) &&
                    // Apply status filter
                    (filter == RequestFilter.ALL ||
                            request.status.equals(filter.name, ignoreCase = true))
        }

        _filteredRequests.value = filtered
    }

    private fun loadAppDetails(requestsList: List<Request>) {
        viewModelScope.launch {
            try {
                val appDetailsMap = mutableMapOf<String, App>()
                requestsList.forEach { request ->
                    val app = appRepository.getAppById(request.appId).getOrNull()
                    if (app != null) {
                        appDetailsMap[request.appId] = app
                    }
                }
                _appDetails.value = appDetailsMap
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to load app details")
            }
        }
    }

    fun refreshRequests() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                requestRepository.syncRequestData()
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to refresh requests")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun togglePinnedStatus(requestId: String)
    {
        viewModelScope.launch {
            try {
                val request = _allRequests.value.find { it.id == requestId } ?: return@launch
                val updatedRequest = request.copy(isPinned = !(request.isPinned ?: false))

                // Update in repository
                requestRepository.updateRequest(updatedRequest)

                // Update local state
                val updatedList = _allRequests.value.map {
                    if (it.id == requestId) updatedRequest else it
                }
                _allRequests.value = updatedList

                // Reapply filters to update the UI
                filterRequests()
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to update pinned status")
            }
        }
    }


    private fun loadProgressData() {
        viewModelScope.launch {
            try {
                val progressMap = mutableMapOf<String, Float>()
                val requests = _allRequests.value

                // If no requests, clear progress map and return
                if (requests.isEmpty()) {
                    _requestProgress.value = emptyMap()
                    return@launch
                }

                for (request in requests) {
                    val progress = ProgressUtils.calculateProgress(request)

                    progressMap[request.id] = progress
                }

                // Update progress map atomically
                _requestProgress.value = progressMap
            } catch (e: Exception) {
                _errorMessage.emit("Failed to calculate request progress")
            }
        }
    }
}