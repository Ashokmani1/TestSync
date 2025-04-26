package com.teksxt.closedtesting.explore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.picked.domain.repo.PickedAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val pickedAppRepository: PickedAppRepository,
    private val requestRepository: RequestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    private val _userOwnApps = MutableStateFlow<Set<String>>(emptySet())

    init {
        loadApps()
        observePickedApps()
        observeUserOwnApps()

        // Filter apps whenever search query changes
        searchQuery
            .debounce(300)
            .onEach { filterApps() }
            .launchIn(viewModelScope)
    }

    private fun observeUserOwnApps() {
        viewModelScope.launch {
            requestRepository.getUserRequests()
                .catch { exception ->
                    _errorState.value = "Failed to load user requests: ${exception.localizedMessage}"
                }
                .collect { requests ->
                    // Extract app IDs from user's requests
                    val userAppIds = requests.map { it.appId }.toSet()
                    _uiState.update { it.copy(userOwnApps = userAppIds) }
                    filterApps()
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: ExploreFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        filterApps()
    }

    fun pickApp(appId: String) {
        viewModelScope.launch {
            try {
                pickedAppRepository.pickApp(appId)
            } catch (e: Exception) {
                _errorState.value = "Failed to pick app: ${e.localizedMessage}"
            }
        }
    }

    fun refreshApps() {
        loadApps()
    }

    fun clearError() {
        _errorState.value = null
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            appRepository.getAllApps().onSuccess { apps ->
                _uiState.update { it.copy(allApps = apps, isLoading = false) }
                filterApps()
            }.onFailure { exception ->
                _errorState.value = "Failed to load apps: ${exception.localizedMessage}"
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observePickedApps() {
        viewModelScope.launch {
            pickedAppRepository.getPickedAppsFlow()
                .catch { exception ->
                    _errorState.value = "Failed to load picked apps: ${exception.localizedMessage}"
                }
                .collect { pickedApps ->
                    _uiState.update { it.copy(pickedApps = pickedApps.data?.map { it.id }?.toSet() ?: emptySet()) }
                }
        }
    }

    private fun filterApps() {
        val query = searchQuery.value
        val filter = uiState.value.selectedFilter
        val allApps = uiState.value.allApps

        val filteredApps = allApps
            ?.filter { app ->
                app.name.contains(query, ignoreCase = true) ||
                        app.description.contains(query, ignoreCase = true)
            }
            ?.filter { app ->
                when (filter) {
                    ExploreFilter.ALL -> true
                    ExploreFilter.AVAILABLE -> (app.activeTesters ?: 0) < (app.totalTesters ?: 0)
                    ExploreFilter.FULL -> (app.activeTesters ?: 0) >= (app.totalTesters ?: 0)
                }
            }

        _uiState.update { it.copy(apps = filteredApps ?: emptyList()) }
    }
}

data class ExploreUiState(
    val allApps: List<App>? = emptyList(),
    val apps: List<App> = emptyList(),
    val pickedApps: Set<String> = emptySet(),
    val userOwnApps: Set<String> = emptySet(),
    val selectedFilter: ExploreFilter = ExploreFilter.ALL,
    val isLoading: Boolean = false
)

enum class ExploreFilter(val displayName: String) {
    ALL("All"),
    AVAILABLE("Available"),
    FULL("Full")
}