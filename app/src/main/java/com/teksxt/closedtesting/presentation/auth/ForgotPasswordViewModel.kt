package com.teksxt.closedtesting.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isResetEmailSent: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun resetPassword() {
        val email = _uiState.value.email

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address") }
            return
        }

        if (!isValidEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isResetEmailSent = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = getFirebaseErrorMessage(e.message ?: "Unknown error")
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun getFirebaseErrorMessage(errorMessage: String): String {
        return when {
            errorMessage.contains("no user record", ignoreCase = true) ->
                "No account found with this email address"
            errorMessage.contains("badly formatted", ignoreCase = true) ->
                "Please enter a valid email address"
            errorMessage.contains("network", ignoreCase = true) ->
                "Network error. Please check your connection"
            errorMessage.contains("INVALID_EMAIL", ignoreCase = true) ->
                "Please enter a valid email address"
            errorMessage.contains("TOO_MANY_ATTEMPTS_TRY_LATER", ignoreCase = true) ->
                "Too many attempts. Please try again later"
            else -> "Failed to send reset email. Please try again"
        }
    }
}