package com.teksxt.closedtesting.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.domain.usecase.auth.LoginUseCase
import com.teksxt.closedtesting.domain.usecase.auth.SignInWithGoogleUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidateEmailUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isGoogleSignInLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events = _events.asSharedFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(
            email = email,
            emailError = null,
            errorMessage = null
        ) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        ) }
    }

    fun login() {
        // Validate inputs first
        val emailResult = validateEmailUseCase(_uiState.value.email)
        val passwordResult = validatePasswordUseCase(_uiState.value.password)

        val hasError = listOf(emailResult, passwordResult).any { !it.successful }

        if (hasError) {
            _uiState.update { it.copy(
                emailError = if (!emailResult.successful) emailResult.errorMessage else null,
                passwordError = if (!passwordResult.successful) passwordResult.errorMessage else null
            ) }
            return
        }

        // Proceed with login
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = loginUseCase(_uiState.value.email, _uiState.value.password)
                if (result.isSuccess) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        loginSuccess = true
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An unexpected error occurred"
                ) }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isGoogleSignInLoading = true,
                errorMessage = null
            )}

            try {
                // Try silent sign-in first
                val result = signInWithGoogleUseCase()

                if (result.isSuccess) {
                    // Silent sign-in succeeded
                    _uiState.update { it.copy(
                        isGoogleSignInLoading = false,
                        loginSuccess = true
                    )}
                    _events.emit(LoginEvent.NavigateToHome)
                } else {
                    // Silent sign-in failed, launch interactive sign-in
                    // Keep loading state true as we're launching the UI
                    _events.emit(LoginEvent.LaunchGoogleSignIn)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isGoogleSignInLoading = false,
                    errorMessage = e.message ?: "An unexpected error occurred"
                )}
            }
        }
    }

    fun handleGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            try {
                // Keep Google sign-in loading state
                _uiState.update { it.copy(isGoogleSignInLoading = true) }

                val result = signInWithGoogleUseCase(idToken)

                if (result.isSuccess) {
                    _uiState.update { it.copy(
                        isGoogleSignInLoading = false,
                        loginSuccess = true
                    )}
                    _events.emit(LoginEvent.NavigateToHome)
                } else {
                    _uiState.update { it.copy(
                        isGoogleSignInLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Google sign-in failed"
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isGoogleSignInLoading = false,
                    errorMessage = e.message ?: "An unexpected error occurred"
                )}
            }
        }
    }

    fun handleGoogleSignInError(errorMessage: String) {
        _uiState.update { it.copy(
            isGoogleSignInLoading = false,
            errorMessage = errorMessage
        )}
    }
    fun resetErrorState() {
        _uiState.update { it.copy(
            errorMessage = null,
            emailError = null,
            passwordError = null
        ) }
    }
}

sealed class LoginEvent {
    object LaunchGoogleSignIn : LoginEvent()
    object NavigateToHome : LoginEvent()
}