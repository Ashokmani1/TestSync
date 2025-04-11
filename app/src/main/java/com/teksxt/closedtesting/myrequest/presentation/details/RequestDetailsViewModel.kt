package com.teksxt.closedtesting.myrequest.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.model.DayTestDetail
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class RequestDetailsViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _assignedTesters = MutableStateFlow<Map<Int, List<AssignedTester>>>(emptyMap())
    val assignedTesters: StateFlow<Map<Int, List<AssignedTester>>> = _assignedTesters.asStateFlow()

    private val _testDetails = MutableStateFlow<Map<Int, List<DayTestDetail>>>(emptyMap())

    private val _selectedDay = MutableStateFlow(1)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Request data
    val request: StateFlow<Request?> = requestRepository.getRequestById(requestId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Calculate progress based on creation date and duration
    val progress: StateFlow<Float> = request.map { request ->
        calculateProgress(request)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    init {
        refreshData()
    }

    private fun loadRequest() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                requestRepository.syncRequests()
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to load request details")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedDay(day: Int) {
        viewModelScope.launch {
            request.value?.let { request ->
                if (day in 1..request.durationInDays) {
                    _selectedDay.value = day
                }
            }
        }
    }

    fun calculateProgress(request: Request?): Float {
        if (request == null) return 0f
        if (request.status.lowercase() == "completed") return 1f

        val createdAt = request.createdAt ?: return 0f
        val currentTime = Date().time
        val elapsedDays = (currentTime - createdAt.time) / (1000 * 60 * 60 * 24)

        return min(1f, max(0f, elapsedDays.toFloat() / request.durationInDays))
    }

    fun refreshData() {
        loadRequest()
        loadAssignedTesters()
        loadTestingData()
    }

    fun getAssignedTesters(dayNumber: Int?): List<AssignedTester> {
        return if (dayNumber == null) {
            // Return all unique testers across all days
            _assignedTesters.value.values.flatten().distinctBy { it.id }
        } else {
            _assignedTesters.value[dayNumber] ?: emptyList()
        }
    }

    // Send reminder to a single tester
    fun sendReminder(dayNumber: Int?, testerId: String) {
        viewModelScope.launch {
            try {
                requestRepository.sendReminder(
                    requestId = requestId,
                    dayNumber = dayNumber,
                    testerId = testerId
                )
                _errorMessage.emit("Reminder sent successfully")
            } catch (e: Exception) {
                _errorMessage.emit("Failed to send reminder: ${e.message}")
            }
        }
    }

    // Send reminders to multiple testers
    fun sendBulkReminders(dayNumber: Int?, testerIds: List<String>)
    {
        viewModelScope.launch {
            try {
                requestRepository.sendBulkReminders(
                    requestId = requestId,
                    dayNumber = dayNumber,
                    testerIds = testerIds
                )
                _errorMessage.emit("Reminders sent successfully")
            } catch (e: Exception) {
                _errorMessage.emit("Failed to send reminders: ${e.message}")
            }
        }
    }

    fun getTestDetailsForDay(day: Int): List<DayTestDetail> {
        // Get test details for the specific day
        return _testDetails.value[day] ?: emptyList()
    }

    fun getAssignedTesters(day: Int): List<AssignedTester> {
        // Get assigned testers for the specific day
        return _assignedTesters.value[day] ?: emptyList()
    }

    // Load testers for the current request
    private fun loadAssignedTesters() {
        viewModelScope.launch {
            try {
                val testers = requestRepository.getAssignedTesters(requestId)
                _assignedTesters.value = testers
            } catch (e: Exception) {
                _errorMessage.emit("Failed to load testers: ${e.message}")
            }
        }
    }

    private fun loadTestingData() {
        viewModelScope.launch {
            try {
                if (requestId.isNotBlank()) {
                    // Load test details
                    val testDetails = requestRepository.getTestDetails(requestId)
                    _testDetails.value = testDetails

                    // Load assigned testers
                    val assignedTesters = requestRepository.getAssignedTesters(requestId)
                    _assignedTesters.value = assignedTesters
                }
            } catch (e: Exception) {
                _errorMessage.emit("Failed to load testing data: ${e.message}")
            }
        }
    }
}