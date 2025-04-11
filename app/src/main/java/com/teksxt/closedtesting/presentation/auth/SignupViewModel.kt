package com.teksxt.closedtesting.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.domain.usecase.auth.SignInWithGoogleUseCase
import com.teksxt.closedtesting.domain.usecase.auth.SignUpUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidateEmailUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidateNameUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignupUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val termsAccepted: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val termsError: String? = null,
    val isLoading: Boolean = false,
    val signupSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateNameUseCase: ValidateNameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null, errorMessage = null) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                errorMessage = null
            )
        }
    }


    fun updateTermsAccepted(accepted: Boolean) {
        _uiState.update { it.copy(termsAccepted = accepted, termsError = null, errorMessage = null) }
    }

    fun signup() {
        // Validate inputs
        val nameResult = validateNameUseCase(_uiState.value.name)
        val emailResult = validateEmailUseCase(_uiState.value.email)
        val passwordResult = validatePasswordUseCase(_uiState.value.password)

        val termsAccepted = _uiState.value.termsAccepted

        // Check for validation errors
        if (!nameResult.successful ||
            !emailResult.successful ||
            !passwordResult.successful ||
            !termsAccepted) {

            _uiState.update { it.copy(
                nameError = if (!nameResult.successful) nameResult.errorMessage else null,
                emailError = if (!emailResult.successful) emailResult.errorMessage else null,
                passwordError = if (!passwordResult.successful) passwordResult.errorMessage else null,
                termsError = if (!termsAccepted) "You must accept the terms and conditions" else null
            ) }
            return
        }

        // Proceed with signup
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = signUpUseCase(
                    name = _uiState.value.name,
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )


                if (result.isSuccess) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        signupSuccess = true
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Signup failed"
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

    fun signUpWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = signInWithGoogleUseCase()
                if (result.isSuccess) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        signupSuccess = true
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Google sign-up failed"
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
}