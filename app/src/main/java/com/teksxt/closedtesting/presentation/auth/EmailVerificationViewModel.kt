package com.teksxt.closedtesting.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teksxt.closedtesting.data.repository.UserPreferencesRepository
import com.teksxt.closedtesting.domain.usecase.auth.EmailVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EmailVerificationState(
    val isEmailVerified: Boolean = false,
    val userEmail: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showBanner: Boolean = true,
    val verificationSent: Boolean = false,
    val lastVerificationSentTimestamp: Long = 0L
)

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val emailVerificationUseCase: EmailVerificationUseCase,
    private val auth: FirebaseAuth,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EmailVerificationState())
    val state: StateFlow<EmailVerificationState> = _state.asStateFlow()

    init {
        loadUserData()
        loadPreferences()
        checkVerificationStatus()
    }

    private fun loadUserData() {
        auth.currentUser?.let { user ->
            _state.update {
                it.copy(
                    userEmail = user.email ?: "",
                    isEmailVerified = user.isEmailVerified
                )
            }
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            // Load banner dismissed state
            userPreferencesRepository.emailVerificationBannerDismissed.collect { dismissed ->
                _state.update { it.copy(showBanner = !dismissed) }
            }
        }

        viewModelScope.launch {
            // Load last sent timestamp
            userPreferencesRepository.verificationLastSentTimestamp.collect { timestamp ->
                _state.update { it.copy(lastVerificationSentTimestamp = timestamp) }
            }
        }
    }

    fun checkVerificationStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // First refresh the user data to get the latest status
                val refreshResult = emailVerificationUseCase.refreshUser()
                if (refreshResult.isFailure) {
                    throw refreshResult.exceptionOrNull() ?: Exception("Failed to refresh user data")
                }

                // Then check if the email is verified
                val verificationResult = emailVerificationUseCase.checkEmailVerified()
                if (verificationResult.isSuccess) {
                    val isVerified = verificationResult.getOrNull() ?: false
                    _state.update {
                        it.copy(
                            isEmailVerified = isVerified,
                            isLoading = false
                        )
                    }

                    // If email is verified, make sure we don't show the banner anymore
                    if (isVerified) {
                        dismissBanner()
                    }
                } else {
                    throw verificationResult.exceptionOrNull() ?: Exception("Failed to check verification status")
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An error occurred checking verification status"
                    )
                }
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            // Don't allow sending emails too frequently (1 minute cooldown)
            val currentTime = System.currentTimeMillis()
            if (currentTime - _state.value.lastVerificationSentTimestamp < 60000) {
                val remainingSeconds = (60000 - (currentTime - _state.value.lastVerificationSentTimestamp)) / 1000
                _state.update {
                    it.copy(
                        errorMessage = "Please wait ${remainingSeconds}s before requesting another verification email"
                    )
                }
                return@launch
            }

            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val result = emailVerificationUseCase.sendVerificationEmail()
                if (result.isSuccess) {
                    val timestamp = System.currentTimeMillis()

                    // Update state
                    _state.update {
                        it.copy(
                            isLoading = false,
                            verificationSent = true,
                            lastVerificationSentTimestamp = timestamp
                        )
                    }

                    // Save timestamp to preferences
                    userPreferencesRepository.setVerificationLastSentTimestamp(timestamp)

                    // Show success message briefly then reset
                    delay(3000)
                    _state.update { it.copy(verificationSent = false) }
                } else {
                    throw result.exceptionOrNull() ?: Exception("Failed to send verification email")
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "An error occurred sending the verification email"
                    )
                }
            }
        }
    }

    fun dismissBanner() {
        viewModelScope.launch {
            // Update the state
            _state.update { it.copy(showBanner = false) }

            // Store the dismissal in preferences
            userPreferencesRepository.setEmailVerificationBannerDismissed(true)
        }
    }

    fun resetErrorState() {
        _state.update { it.copy(errorMessage = null) }
    }
}