package com.teksxt.closedtesting.myrequest.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
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
    private val appRepository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditRequestUiState())
    val uiState: StateFlow<EditRequestUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditRequestEvent>()
    val events: SharedFlow<EditRequestEvent> = _events.asSharedFlow()

    private var originalRequest: Request? = null

    val appCategories = listOf(
        "Games", "Education", "Business", "Entertainment",
        "Finance", "Health & Fitness", "Lifestyle", "Productivity",
        "Social", "Tools", "Travel", "Music & Audio", "Photography",
        "Shopping", "Sports", "Weather", "Other"
    )


    fun loadRequest(requestId: String)
    {
        viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true, isRequestLoaded = false) }

            try {
                requestRepository.getRequestByIdFlow(requestId).collect { resource ->

                    if (resource.data != null)
                    {
                        originalRequest = resource.data

                        val appDetails = appRepository.getAppById(resource.data.appId).getOrNull()

                        _uiState.update { state ->
                            state.copy(
                                title = originalRequest?.title ?: "",
                                description = originalRequest?.description ?: "",
                                appCategory = appDetails?.category ?: "",
                                testingInstructions = appDetails?.testingInstructions ?: "",
                                promoCode = appDetails?.premiumCode ?: "",
                                isPremium = appDetails?.premiumCode != null,
                                isRequestLoaded = true,
                                isFormValid = true
                            )
                        }
                    } else {
                        _events.emit(EditRequestEvent.ShowError("Request not found"))
                    }
                }
            } catch (e: Exception) {
                _events.emit(EditRequestEvent.ShowError(e.message ?: "Error loading request"))
            } finally {
                _uiState.update { it.copy(isLoading = false, isRequestLoaded = false) }
            }
        }
    }

    // Update form fields
    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value, titleError = if (value.isBlank()) "Title cannot be empty" else null,  isFormValid = validateForm(
            value, it.description, it.appCategory,
            it.promoCode, it.isPremium
        ))  }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value, isFormValid = validateForm(
                it.title, value, it.appCategory,
                it.promoCode, it.isPremium
            ))
        }
    }


    fun updateAppCategory(value: String) {
        _uiState.update { it.copy(
            appCategory = value,
            appCategoryError = null,
            isFormValid = validateForm(
                it.title, it.description, value,
                it.promoCode, it.isPremium
            )
        )}
    }

    fun updatePromoCode(value: String) {
        _uiState.update { it.copy(
            promoCode = value,
            promoCodeError = null,
            isFormValid = validateForm(
                it.title, it.description, it.appCategory,
                value, it.isPremium
            )
        )}
    }

    fun updateTestingInstructions(value: String) {
        _uiState.update { it.copy(
            testingInstructions = value,
            isFormValid = validateForm(
                it.title, it.description, it.appCategory,
                it.promoCode, it.isPremium
            )
        )}
    }

    fun setCategoryDropdownExpanded(value: Boolean) {
        _uiState.update { it.copy(isCategoryDropdownExpanded = value) }
    }


    private fun validateForm(
        title: String,
        description: String,
        appCategory: String,
        promoCode: String,
        isPremium: Boolean
    ): Boolean {
        var isValid = true
        var titleError: String? = null
        var descriptionError: String? = null
        var appCategoryError: String? = null
        var promoCodeError: String? = null

        if (title.isBlank()) {
            titleError = "Title cannot be empty"
            isValid = false
        }

        if (description.isBlank()) {
            descriptionError = "Description cannot be empty"
            isValid = false
        }

        if (appCategory.isBlank()) {
            appCategoryError = "Please select an app category"
            isValid = false
        }

        if (isPremium && promoCode.isBlank()) {
            promoCodeError = "Promo code is required for premium apps"
            isValid = false
        }

        _uiState.update { it.copy(
            titleError = titleError,
            descriptionError = descriptionError,
            appCategoryError = appCategoryError,
            promoCodeError = promoCodeError
        )}

        return isValid
    }


    fun updateRequest()
    {
        val currentState = _uiState.value
        if (!validateForm(currentState.title, currentState.description, currentState.appCategory, currentState.promoCode, currentState.isPremium)) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {

                val originalRequest = originalRequest ?: throw IllegalStateException("Original request is null")

                // Get existing request
                val existingApp = appRepository.getAppById(originalRequest.appId).getOrNull()

                if (existingApp != null) {
                    // Update request
                    val updatedRequest = originalRequest.copy(
                        title = currentState.title,
                        description = currentState.description,
                        updatedAt = System.currentTimeMillis()
                    )

                    // Update app
                    val updatedApp = existingApp.copy(
                        name = currentState.title,
                        description = currentState.description,
                        category = currentState.appCategory,
                        testingInstructions = currentState.testingInstructions,
                        premiumCode = if (currentState.isPremium) currentState.promoCode else null
                    )

                    // Save both updates
                    requestRepository.updateRequest(updatedRequest)

                    appRepository.updateApp(updatedApp)

                    _events.emit(EditRequestEvent.RequestUpdated(originalRequest.id))
                } else {
                    _events.emit(EditRequestEvent.ShowError("Request not found"))
                }
            } catch (e: Exception) {
                _events.emit(EditRequestEvent.ShowError(e.message ?: "Error updating request"))
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
    // Add these new fields
    val appCategory: String = "",
    val testingInstructions: String = "",
    val promoCode: String = "",
    val isPremium: Boolean = false,
    val isCategoryDropdownExpanded: Boolean = false,

    // Add error states
    val titleError: String? = null,
    val descriptionError: String? = null,
    val appCategoryError: String? = null,
    val promoCodeError: String? = null,

    val isLoading: Boolean = false,
    val isRequestLoaded: Boolean = false,
    val isFormValid: Boolean = false
)

sealed class EditRequestEvent {
    data class RequestUpdated(val requestId: String) : EditRequestEvent()
    data class ShowError(val message: String) : EditRequestEvent()
}