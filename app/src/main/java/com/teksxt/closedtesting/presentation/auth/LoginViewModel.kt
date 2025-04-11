package com.teksxt.closedtesting.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.domain.usecase.auth.LoginUseCase
import com.teksxt.closedtesting.domain.usecase.auth.SignInWithGoogleUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidateEmailUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = signInWithGoogleUseCase()
                if (result.isSuccess) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        loginSuccess = true
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Google sign-in failed"
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

    fun resetErrorState() {
        _uiState.update { it.copy(
            errorMessage = null,
            emailError = null,
            passwordError = null
        ) }
    }
}