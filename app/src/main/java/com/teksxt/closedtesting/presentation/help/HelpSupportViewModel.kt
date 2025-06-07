package com.teksxt.closedtesting.presentation.help

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.core.util.Resource
import com.teksxt.closedtesting.domain.usecase.help.SubmitContactFormUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpSupportViewModel @Inject constructor(
    private val submitContactFormUseCase: SubmitContactFormUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HelpSupportUiState())
    val uiState = _uiState.asStateFlow()


    fun updateContactName(name: String) {
        _uiState.update { it.copy(contactName = name) }
    }

    fun updateContactEmail(email: String) {
        _uiState.update { it.copy(contactEmail = email) }
    }

    fun updateContactSubject(subject: String) {
        _uiState.update { it.copy(contactSubject = subject) }
    }

    fun updateContactMessage(message: String) {
        _uiState.update { it.copy(contactMessage = message) }
    }

    fun submitContactForm() {
        viewModelScope.launch {
            // Basic validation
            val currentState = _uiState.value
            if (currentState.contactName.isBlank() ||
                currentState.contactEmail.isBlank() ||
                currentState.contactSubject.isBlank() ||
                currentState.contactMessage.isBlank()) {

                _uiState.update { it.copy(
                    errorMessage = "Please fill in all fields"
                )}
                return@launch
            }

            // Email validation
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.contactEmail).matches()) {
                _uiState.update { it.copy(
                    errorMessage = "Please enter a valid email address"
                )}
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }

            try {
                val result = submitContactFormUseCase(
                    name = currentState.contactName,
                    email = currentState.contactEmail,
                    subject = currentState.contactSubject,
                    message = currentState.contactMessage,
                )

                when(result)
                {
                    is Resource.Error -> {
                        _uiState.update { it.copy(
                            isSubmitting = false,
                            errorMessage = result.message ?: "An unknown error occurred"
                        )}
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSubmitting = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(
                            isSubmitting = false,
                            successMessage = "Thank you! Your message has been sent successfully. Our support team will contact you shortly.",
                            contactName = "",
                            contactEmail = "",
                            contactSubject = "",
                            contactMessage = ""
                        )}
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "An unknown error occurred"
                )}
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

data class HelpSupportUiState(
    val contactName: String = "",
    val contactEmail: String = "",
    val contactSubject: String = "",
    val contactMessage: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
