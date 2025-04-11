package com.teksxt.closedtesting.assignedtests.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.assignedtests.domain.model.AssignedTest
import com.teksxt.closedtesting.assignedtests.domain.repo.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignedTestsViewModel @Inject constructor(
    private val testRepository: TestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignedTestsUiState())
    val uiState: StateFlow<AssignedTestsUiState> = _uiState.asStateFlow()

    init {
        loadAssignedTests()
    }

    private fun loadAssignedTests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val tests = testRepository.getAssignedTests()
                _uiState.update {
                    it.copy(
                        assignedTests = tests,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load assigned tests",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AssignedTestsUiState(
    val assignedTests: List<AssignedTest> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)