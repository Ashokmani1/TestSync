package com.teksxt.closedtesting.picked.presentation.details

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.chat.domain.repository.ChatRepository
import com.teksxt.closedtesting.core.util.Resource
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.myrequest.domain.model.TestingStatus
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.picked.domain.model.PickedApp
import com.teksxt.closedtesting.picked.domain.repo.PickedAppRepository
import com.teksxt.closedtesting.picked.presentation.list.PickedAppWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickedAppDetailsViewModel @Inject constructor(
    private val pickedAppRepository: PickedAppRepository,
    private val appRepository: AppRepository,
    private val feedbackRepository: ChatRepository,
    private val requestRepository: RequestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PickedAppDetailsState(isLoading = true))
    val state: StateFlow<PickedAppDetailsState> = _state.asStateFlow()


    private val _feedbackState = MutableStateFlow(FeedbackState())
    val feedbackState: StateFlow<FeedbackState> = _feedbackState.asStateFlow()

    // Add this property to track the selected day
    private val _selectedDay = MutableStateFlow<Int?>(null)
    val selectedDay = _selectedDay.asStateFlow()

    // Add this property to store day-wise feedback
    private val _dayWiseFeedback = MutableStateFlow<Map<Int?, List<ChatMessage>>>(emptyMap())
    val dayWiseFeedback = _dayWiseFeedback.asStateFlow()


    // Store requestId and userId for feedback operations
    private var requestId: String? = null

    private var userId: String? = null

    private var ownerUserID: String? = null

    init {
        savedStateHandle.get<String>("pickedAppId")?.let { pickedAppId ->
            loadPickedAppDetails(pickedAppId)
        }
    }

    fun setSelectedDay(day: Int?) {
        _selectedDay.value = day
    }

    fun loadPickedAppDetails(pickedAppId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            pickedAppRepository.getPickedAppById(pickedAppId).onSuccess { pickedApp ->
                pickedApp?.let {
                    _state.value = _state.value.copy(
                        pickedApp = it,
                        isLoading = false
                    )

                    userId = it.userId

                    loadRequestId(it.appId, it.userId)

                    // Fetch app details to get additional information
                    loadAppDetails(it.appId)
                }
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    error = error.message ?: "Failed to load app details",
                    isLoading = false
                )
            }
        }
    }

    private fun loadRequestId(appId: String, userId: String) {
        viewModelScope.launch {
            // Find the request associated with this app and user
            requestRepository.getRequestByAppID(appId).onSuccess { request ->

                requestId = request?.id

                ownerUserID = request?.ownerUserId

                // Now that we have the requestId, load feedback history
                requestId?.let {
                    loadFeedbackHistory()
                }
            }
        }
    }

    private fun loadAppDetails(appId: String) {
        viewModelScope.launch {
            appRepository.getAppById(appId).onSuccess { app ->
                app?.let {
                    _state.value = _state.value.copy(
                        app = it
                    )
                }
            }
        }
    }

    fun togglePinnedStatus() {
        val pickedApp = _state.value.pickedApp ?: return
        viewModelScope.launch {
            pickedAppRepository.togglePickedAppPin(pickedApp.id).onSuccess {
                // Reload the picked app to get updated data
                loadPickedAppDetails(pickedApp.id)
            }
        }
    }

    fun updateStatus(newStatus: String) {
        val pickedApp = _state.value.pickedApp ?: return
        viewModelScope.launch {
            pickedAppRepository.updatePickedAppStatus(
                pickedApp.id,
                newStatus
            ).onSuccess {

                if (newStatus == "COMPLETED") {
                    pickedAppRepository.updatePickedAppProgress(
                        pickedApp.id,
                        1.0f, // 100% completion
                        pickedApp.currentTestDay
                    )
                    requestId?.let { it1 -> userId?.let { testerId -> requestRepository.updateTesterDayStatus(it1, testerId, pickedApp.currentTestDay, TestingStatus.COMPLETED) } }
                }
                else
                {
                    pickedAppRepository.updatePickedAppProgress(
                        pickedApp.id,
                        0.0f,
                        pickedApp.currentTestDay
                    )
                    requestId?.let { it1 -> userId?.let { testerId -> requestRepository.updateTesterDayStatus(it1, testerId, pickedApp.currentTestDay, TestingStatus.IN_PROGRESS) } }
                }

                // Reload the picked app to get updated data
                loadPickedAppDetails(pickedApp.id)
            }
        }
    }

    fun unpickApp(onSuccess: () -> Unit) {
        val pickedApp = _state.value.pickedApp ?: return
        viewModelScope.launch {
            pickedAppRepository.unpickApp(pickedApp.id).onSuccess {
                onSuccess()
            }
        }
    }

    private fun loadFeedbackHistory() {
        viewModelScope.launch {
            _feedbackState.value = _feedbackState.value.copy(isLoading = true)

            try {
                feedbackRepository.getMessagesForRequestAndUsers(
                    requestId = requestId ?: "",
                    userId1 = userId ?: "",
                    userId2 = ownerUserID ?: ""
                ).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {

                            val feedbacks = resource.data ?: emptyList()

                            // Group feedback by test day
                            val groupedFeedback = feedbacks.groupBy { it.dayNumber }
                            _dayWiseFeedback.value = groupedFeedback

                            _feedbackState.value = _feedbackState.value.copy(
                                feedback = feedbacks,
                                isLoading = false
                            )
                        }
                        is Resource.Error -> {
                            _feedbackState.value = _feedbackState.value.copy(error = (resource.message ?: "Failed to load messages"))
                        }
                        is Resource.Loading -> {
                            _feedbackState.value = _feedbackState.value.copy(isLoading = true)
                        }
                    }
                    _feedbackState.value = _feedbackState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _feedbackState.value = _feedbackState.value.copy(error = e.message ?: "Failed to load messages")
                _feedbackState.value = _feedbackState.value.copy(isLoading = false)
            }
        }
    }


    fun submitFeedback(text: String, screenshots: List<Uri>, day: Int)
    {
        viewModelScope.launch {
            val pickedApp = state.value.pickedApp ?: return@launch
            val currentRequestId = requestId
            val currentUserId = ownerUserID

            if (currentRequestId == null || currentUserId == null) {
                _feedbackState.value = _feedbackState.value.copy(
                    isSubmitting = false,
                    error = "Missing request information. Please try again."
                )
                return@launch
            }

            _feedbackState.value = _feedbackState.value.copy(isSubmitting = true)

            try {

                // TODO need to handle file handling
                // Submit feedback with screenshots
                feedbackRepository.createFeedbackMessage(currentRequestId, currentUserId, day, text).onSuccess {
                    // Refresh feedback list
                    loadFeedbackHistory()
                    _feedbackState.value = _feedbackState.value.copy(
                        isSubmitting = false,
                        submitSuccess = true
                    )
                }.onFailure { error ->
                    _feedbackState.value = _feedbackState.value.copy(
                        isSubmitting = false,
                        error = error.message ?: "Failed to submit feedback"
                    )
                }
            } catch (e: Exception) {
                _feedbackState.value = _feedbackState.value.copy(
                    isSubmitting = false,
                    error = e.message ?: "Failed to submit feedback"
                )
            }
        }
    }

    // Clear feedback submit success state
    fun clearSubmitSuccess() {
        _feedbackState.value = _feedbackState.value.copy(submitSuccess = false)
    }

    // Clear error
    fun clearError() {
        _feedbackState.value = _feedbackState.value.copy(error = null)
    }
}