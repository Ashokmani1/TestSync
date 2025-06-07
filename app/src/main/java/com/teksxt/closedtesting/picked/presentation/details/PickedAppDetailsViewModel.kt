package com.teksxt.closedtesting.picked.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.myrequest.domain.model.TestingStatus
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.picked.domain.repo.PickedAppRepository
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
    private val requestRepository: RequestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(PickedAppDetailsState(isLoading = true))
    val state: StateFlow<PickedAppDetailsState> = _state.asStateFlow()

    private val _testingStatus = MutableStateFlow<TestingStatus>(TestingStatus.PENDING)
    val testingStatus: StateFlow<TestingStatus> = _testingStatus.asStateFlow()


    // Store requestId and userId for feedback operations
    internal var requestId: String? = null

    private var userId: String? = null

    internal var ownerUserID: String? = null

    init {
        savedStateHandle.get<String>("pickedAppId")?.let { pickedAppId ->
            loadPickedAppDetails(pickedAppId)
        }
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

                    loadRequestId(it.appId)

                    loadAppDetails(it.appId)

                    if (requestId != null && userId != null)
                    {
                        loadTesterDayStatus(requestId!!, userId!!, it.currentTestDay)
                    }
                }
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    error = error.message ?: "Failed to load app details",
                    isLoading = false
                )
            }
        }
    }

    private fun loadRequestId(appId: String) {
        viewModelScope.launch {
            // Find the request associated with this app and user
            requestRepository.getRequestByAppID(appId).onSuccess { request ->

                requestId = request?.id

                ownerUserID = request?.ownerUserId
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

    private fun loadTesterDayStatus(requestId: String, testerId: String, dayNumber: Int) {
        viewModelScope.launch {
            requestRepository.getTesterDayStatus(requestId, testerId, dayNumber).onSuccess { status ->
                _testingStatus.value = status

                // Update the local status in picked app if it doesn't match
                val currentStatus = when (status) {
                    TestingStatus.COMPLETED -> "COMPLETED"
                    TestingStatus.IN_PROGRESS -> "ACTIVE"
                    else -> "ACTIVE"
                }

                if (_state.value.pickedApp?.status != currentStatus) {
                    updateStatus(currentStatus, false) // Don't update Firestore, just sync local state
                }
            }
        }
    }

    fun updateStatus(newStatus: String, updateFirestore: Boolean = true) {
        val pickedApp = _state.value.pickedApp ?: return
        viewModelScope.launch {
            if (updateFirestore) {
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
                        requestId?.let { it1 ->
                            userId?.let { testerId ->
                                requestRepository.updateTesterDayStatus(it1, testerId, pickedApp.currentTestDay, TestingStatus.COMPLETED)
                            }
                        }
                    } else {
                        pickedAppRepository.updatePickedAppProgress(
                            pickedApp.id,
                            0.0f,
                            pickedApp.currentTestDay
                        )
                        requestId?.let { it1 ->
                            userId?.let { testerId ->
                                requestRepository.updateTesterDayStatus(it1, testerId, pickedApp.currentTestDay, TestingStatus.IN_PROGRESS)
                            }
                        }
                    }

                    // Reload the picked app to get updated data
                    loadPickedAppDetails(pickedApp.id)
                }
            } else {
                // Just update local UI state without touching Firestore
                _state.value = _state.value.copy(
                    pickedApp = pickedApp.copy(status = newStatus)
                )
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
}