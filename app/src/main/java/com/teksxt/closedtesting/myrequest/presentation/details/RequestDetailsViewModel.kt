package com.teksxt.closedtesting.myrequest.presentation.details

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.chat.domain.repository.ChatRepository
import com.teksxt.closedtesting.core.util.ProgressUtils
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class RequestDetailsViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val chatRepository: ChatRepository,
    private val appRepository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _assignedTesters = MutableStateFlow<Map<Int, List<AssignedTester>>>(emptyMap())

    private val _selectedDay = MutableStateFlow(1)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    // Add this to the top of your ViewModel with other state definitions
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Add this property near your other state definitions
    private val _appDetails = MutableStateFlow<App?>(null)
    val appDetails: StateFlow<App?> = _appDetails.asStateFlow()

    private val _scrollToPosition = MutableSharedFlow<Int>()
    val scrollToPosition: SharedFlow<Int> = _scrollToPosition.asSharedFlow()

    private val _sendBulkReminderLoading = MutableStateFlow(false)
    val sendBulkReminderLoading: StateFlow<Boolean> = _sendBulkReminderLoading.asStateFlow()

    // Request data with proper Resource handling
    private val _requestResource = MutableStateFlow<Resource<Request?>>(Resource.Loading())
    val request = _requestResource.map { resource ->
        when(resource) {
            is Resource.Success -> resource.data
            else -> null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Calculate progress based on creation date and duration
    val progress: StateFlow<Float> = request
        .map { request ->
            request?.let { ProgressUtils.calculateProgress(it) } ?: 0f
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    init {
        refreshData()
    }

    private fun getRequestWithCalculatedCurrentDay(request: Request): Request {
        val calculatedCurrentDay = ProgressUtils.calculateCurrentDay(request)
        return if (calculatedCurrentDay != request.currentDay) {
            request.copy(currentDay = calculatedCurrentDay)
        } else {
            request
        }
    }

    private fun loadRequest() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                requestRepository.getRequestByIdFlow(requestId).collect { resource ->

                    _isLoading.value = resource !is Resource.Loading

                    // Load app details when request is loaded
                    resource.data?.let { request ->

                        val updatedRequest = getRequestWithCalculatedCurrentDay(request)
                        _requestResource.value = Resource.Success(updatedRequest)

                        loadAppDetails(request.appId)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to load request details")
                _isLoading.value = false
            }
        }
    }

    // Add this new method to load app details
    private fun loadAppDetails(appId: String) {
        viewModelScope.launch {
            try {
                val appResult = appRepository.getAppById(appId)
                if (appResult.isSuccess) {
                    _appDetails.value = appResult.getOrNull()
                } else {
                    _errorMessage.emit("Failed to load app details")
                }
            } catch (e: Exception) {
                _errorMessage.emit("Error loading app details: ${e.message}")
            }
        }
    }

    fun setSelectedDay(day: Int) {
        viewModelScope.launch {
            request.value?.let { request ->
                if (day in 1..request.testingDays) {
                    _selectedDay.value = day
                }
            }
        }
    }


    fun refreshData() {
        loadRequest()
        loadAssignedTesters()
    }

    private fun loadAssignedTesters() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = requestRepository.getAssignedTesters(requestId)

                if (result.isSuccess) {
                    _assignedTesters.value = result.getOrNull() ?: emptyMap()

                    // If we have testers but no selectedDay, set to first day with testers
                    if (selectedDay.value == 0 && _assignedTesters.value.isNotEmpty()) {
                        val firstDayWithTesters = _assignedTesters.value.keys.minOrNull() ?: 1
                        setSelectedDay(firstDayWithTesters)
                    }
                } else {
                    _errorMessage.emit(
                        result.exceptionOrNull()?.message ?: "Failed to load testers"
                    )
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to load testers")
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun getAssignedTesters(dayNumber: Int?): List<AssignedTester> {
        return if (dayNumber == null) {
            // Return all unique testers across all days
            _assignedTesters.value.values.flatten().distinctBy { it.id }
        } else {
            _assignedTesters.value[dayNumber] ?: emptyList()
        }
    }


    // Send reminders to multiple testers
    fun sendBulkReminders(dayNumber: Int?) {
        viewModelScope.launch {
            try {
                _sendBulkReminderLoading.value = true
                val testerIds = getAssignedTesters(dayNumber)
                    .filter { !it.hasCompleted }
                    .map { it.id }

                if (testerIds.isEmpty()) {
                    _errorMessage.emit("No pending testers to remind")
                    return@launch
                }

                val result = chatRepository.sendBulkReminders(
                    requestId = requestId,
                    dayNumber = dayNumber,
                    testerIds = testerIds
                )

                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    _errorMessage.emit("Reminders sent to $count testers")
                } else {
                    _errorMessage.emit(
                        "Failed to send reminders: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _errorMessage.emit("Failed to send reminders: ${e.message}")
            } finally {
                _sendBulkReminderLoading.value = false
            }
        }
    }

    fun sendReminder(testerId: String, dayNumber: Int?) {
        viewModelScope.launch {
            try {
                val result = chatRepository.sendReminderMessage(
                    requestId = requestId,
                    dayNumber = dayNumber,
                    testerId = testerId
                )

                if (result.isFailure) {
                    _errorMessage.emit("Failed to send reminder")
                } else {
                    _errorMessage.emit("Reminder sent successfully")
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to send reminder")
            }
        }
    }

    fun shareAppDetails(context: Context) {
        viewModelScope.launch {
            try {
                val request = request.value ?: return@launch
                val app = appDetails.value

                val shareText = buildString {
                    append("🧪 You're Invited to Test: ${app?.name ?: request.title}\n\n")

                    // App Details
                    append("📱 App Details\n\n")

                    append("Name: ${app?.name ?: "N/A"}\n")

                    // Description
                    request.description?.takeIf { it.isNotBlank() }?.let {
                        append("📝 $it\n\n")
                    }

                    // Play Store URL
                    app?.playStoreUrl?.takeIf { it.isNotBlank() }?.let {
                        append("📦 Download from Play Store:\n$it\n\n")
                    }

                    // Web Testing Link (Opt-in)
                    app?.testApkUrl?.takeIf { it.isNotBlank() }?.let {
                        append("🌐 Join Web Testing (Opt-in):\n$it\n\n")
                    }

                    // Tester Group
                    app?.googleGroupUrl?.takeIf { it.isNotBlank() }?.let {
                        append("👥 Join Our Tester Community:\n$it\n\n")
                    } ?: append("👥 Join Our Tester Community:\nContact the organizer for access.\n\n")

                    app?.premiumCode?.takeIf { it.isNotBlank() }?.let {
                        append("🔐 Premium Code:\n$it\n\n")
                    }

                    // Optional: Test Instructions
                    app?.testingInstructions?.takeIf { it.isNotBlank() }?.let {
                        append("🧾 Test Instructions:\n$it\n\n")
                    }

                    // Footer
                    append("📤 Shared via TestSync App — download now and simplify your testing experience!")
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Join testing for ${app?.name ?: request.title}")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share Testing Invitation"))
            } catch (e: Exception) {
                _errorMessage.emit("Failed to share: ${e.message}")
            }
        }
    }

    // Delete request function
    fun deleteRequest() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = requestRepository.deleteRequest(requestId)
                if (result.isSuccess) {
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                } else {
                    _errorMessage.emit(
                        result.exceptionOrNull()?.message ?: "Failed to delete request"
                    )
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to delete request")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Edit request function
    fun editRequest() {
        viewModelScope.launch {
            try {
               _navigationEvent.emit(NavigationEvent.NavigateToEditRequest(requestId))
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to navigate to edit screen")
            }
        }
    }

    fun scrollToTesters() {
        viewModelScope.launch {

            _scrollToPosition.emit(2)
        }
    }
}

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
    data class NavigateToEditRequest(val requestId: String) : NavigationEvent()
}