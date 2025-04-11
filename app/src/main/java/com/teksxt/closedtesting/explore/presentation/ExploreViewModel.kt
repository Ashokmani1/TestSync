package com.teksxt.closedtesting.explore.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.explore.domain.model.AppInfo
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadApps()
        observePickedApps()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterApps()
    }

    fun onFilterChange(filter: ExploreFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        filterApps()
    }

    fun pickApp(appId: String) {
        viewModelScope.launch {
            try {
                appRepository.pickApp(appId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            appRepository.getAllApps()
                .catch { /* Handle error */ }
                .collect { apps ->
                    _uiState.update { it.copy(allApps = apps) }
                    filterApps()
                }
        }
    }

    private fun observePickedApps() {
        viewModelScope.launch {
            appRepository.getPickedApps()
                .collect { pickedApps ->
                    _uiState.update { it.copy(pickedApps = pickedApps.toSet()) }
                }
        }
    }

    private fun filterApps() {
        val query = searchQuery.value
        val filter = uiState.value.selectedFilter
        val allApps = uiState.value.allApps

        val filteredApps = allApps
            .filter { app ->
                app.name.contains(query, ignoreCase = true) ||
                        app.description.contains(query, ignoreCase = true)
            }
            .filter { app ->
                when (filter) {
                    ExploreFilter.ALL -> true
                    ExploreFilter.AVAILABLE -> app.currentTesters < app.requiredTesters
                    ExploreFilter.FULL -> app.currentTesters >= app.requiredTesters
                }
            }

        _uiState.update { it.copy(apps = filteredApps) }
    }
}

data class ExploreUiState(
    val allApps: List<AppInfo> = emptyList(),
    val apps: List<AppInfo> = emptyList(),
    val pickedApps: Set<String> = emptySet(),
    val selectedFilter: ExploreFilter = ExploreFilter.ALL
)

enum class ExploreFilter(val displayName: String) {
    ALL("All"),
    AVAILABLE("Available"),
    FULL("Full")
}