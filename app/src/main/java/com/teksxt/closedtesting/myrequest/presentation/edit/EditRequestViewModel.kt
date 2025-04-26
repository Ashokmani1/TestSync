package com.teksxt.closedtesting.myrequest.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditRequestViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditRequestUiState())
    val uiState: StateFlow<EditRequestUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditRequestEvent>()
    val events: SharedFlow<EditRequestEvent> = _events.asSharedFlow()

    private var originalRequest: Request? = null

    // Load request to edit
    fun loadRequest(requestId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                requestRepository.getRequestByIdFlow(requestId).collect { resource ->
                    if (resource.data != null) {
                        originalRequest = resource.data

                        _uiState.update { state ->
                            state.copy(
                                requestId = resource.data.id,
                                title = resource.data.title,
                                description = resource.data.description ?: "",
                                isLoading = false,
                                isRequestLoaded = true
                            )
                        }

                        validateForm()
                    }
                }
            } catch (e: Exception) {
                _events.emit(EditRequestEvent.ShowError(e.message ?: "Failed to load request"))
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Update form fields
    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value, titleError = if (value.isBlank()) "Title cannot be empty" else null) }
        validateForm()
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
        validateForm()
    }


    private fun validateForm() {
        val isValid = with(_uiState.value) {
            title.isNotBlank() && titleError == null
        }

        _uiState.update { it.copy(isFormValid = isValid) }
    }

    // Update request
    fun updateRequest() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val originalRequest = originalRequest ?: throw IllegalStateException("Original request is null")

                val updatedRequest = originalRequest.copy(
                    title = state.title,
                    description = state.description,
                    updatedAt = System.currentTimeMillis()
                )

                val result = requestRepository.updateRequest(updatedRequest)

                if (result.isSuccess) {
                    _events.emit(EditRequestEvent.RequestUpdated(updatedRequest.id))
                } else {
                    _events.emit(EditRequestEvent.ShowError(
                        result.exceptionOrNull()?.message ?: "Failed to update request"
                    ))
                }
            } catch (e: Exception) {
                _events.emit(EditRequestEvent.ShowError(e.message ?: "Failed to update request"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class EditRequestUiState(
    val requestId: String = "",
    val title: String = "",
    val description: String = "",
    val titleError: String? = null,
    val descriptionError: String? = null,
    val isLoading: Boolean = false,
    val isRequestLoaded: Boolean = false,
    val isFormValid: Boolean = false
)

sealed class EditRequestEvent {
    data class RequestUpdated(val requestId: String) : EditRequestEvent()
    data class ShowError(val message: String) : EditRequestEvent()
}