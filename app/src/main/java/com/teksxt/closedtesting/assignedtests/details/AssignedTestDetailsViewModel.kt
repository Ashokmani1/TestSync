package com.teksxt.closedtesting.assignedtests.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.assignedtests.domain.model.AssignedTest
import com.teksxt.closedtesting.assignedtests.domain.model.DayTest
import com.teksxt.closedtesting.assignedtests.domain.repo.TestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignedTestDetailsViewModel @Inject constructor(
    private val testRepository: TestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val testId: String = checkNotNull(savedStateHandle.get<String>("testId"))

    private val _uiState = MutableStateFlow(AssignedTestDetailsUiState())
    val uiState: StateFlow<AssignedTestDetailsUiState> = _uiState.asStateFlow()

    init {
        loadTestDetails()
    }

    private fun loadTestDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val test = testRepository.getTestById(testId)
                val dayTests = testRepository.getDayTests(testId)

                _uiState.update {
                    it.copy(
                        test = test,
                        dayTests = dayTests,
                        totalDays = test.totalDays,
                        currentDay = getCurrentDayIndex(test),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load test details",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun getCurrentDayIndex(test: AssignedTest): Int {
        // Calculate current day based on start date and current date
        // For simplicity, we'll just return completed days + 1 or total days if completed
        return if (test.status == "COMPLETED") test.totalDays else test.completedDays + 1
    }

    fun setSelectedDay(day: Int) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    fun uploadScreenshot(imageUri: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val day = uiState.value.selectedDay
                testRepository.uploadScreenshot(testId, day, imageUri)
                loadTestDetails() // Reload test details
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to upload screenshot",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun submitFeedback(feedback: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val day = uiState.value.selectedDay
                testRepository.submitFeedback(testId, day, feedback)
                loadTestDetails() // Reload test details
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to submit feedback",
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

data class AssignedTestDetailsUiState(
    val test: AssignedTest? = null,
    val dayTests: List<DayTest> = emptyList(),
    val totalDays: Int = 0,
    val currentDay: Int = 1,
    val selectedDay: Int = 1,
    val isLoading: Boolean = false,
    val error: String? = null
)