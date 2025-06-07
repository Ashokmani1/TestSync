package com.teksxt.closedtesting.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.presentation.auth.SessionManager
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val navigationEvent: SplashNavigationEvent? = null
)

sealed class SplashNavigationEvent {
    object NavigateToHome : SplashNavigationEvent()
    object NavigateToLogin : SplashNavigationEvent()
    object NavigateToOnboarding : SplashNavigationEvent()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        checkAuthenticationState()
    }

    private fun checkAuthenticationState() {
        viewModelScope.launch {
            try {
                // First check if user is logged in
                if (sessionManager.isUserLoggedIn) {
                    // Try to refresh user data silently

                    sessionManager.startTrackingUserActivity()

                    try {
                        sessionManager.refreshUserData()
                    } catch (e: Exception) {
                        // If refresh fails, continue with locally cached data
                    }

                    // Check if onboarding is completed
                    val onboardingCompleted = sessionManager.isUserOnboarded()

                    if (onboardingCompleted) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                navigationEvent = SplashNavigationEvent.NavigateToHome
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                navigationEvent = SplashNavigationEvent.NavigateToOnboarding
                            )
                        }
                    }
                } else {
                    // User is not logged in
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigationEvent = SplashNavigationEvent.NavigateToLogin
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error checking authentication: ${e.message}",
                        navigationEvent = SplashNavigationEvent.NavigateToLogin
                    )
                }
            }
        }
    }
}