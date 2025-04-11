package com.teksxt.closedtesting.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.data.auth.AuthState
import com.teksxt.closedtesting.data.auth.SessionManager
import com.teksxt.closedtesting.data.preferences.UserPreferencesManager
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
    private val sessionManager: SessionManager,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        checkAuthenticationState()
    }

    private fun checkAuthenticationState() {
        viewModelScope.launch {
            try {
                when (sessionManager.authState.value) {
                    is AuthState.Authenticated -> {
                        // Check if onboarding is completed
                        val onboardingCompleted = preferencesManager.onboardingCompleted.first()
                        if (onboardingCompleted) {
                            _uiState.update {
                                it.copy(navigationEvent = SplashNavigationEvent.NavigateToHome)
                            }
                        } else {
                            _uiState.update {
                                it.copy(navigationEvent = SplashNavigationEvent.NavigateToOnboarding)
                            }
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        _uiState.update {
                            it.copy(navigationEvent = SplashNavigationEvent.NavigateToLogin)
                        }
                    }
                    is AuthState.Loading -> {
                        // Wait for the auth state to stabilize
                        // This will be handled in a subsequent collectLatest block
                    }
                    is AuthState.Error -> {
                        val error = (sessionManager.authState.value as AuthState.Error).message
                        _uiState.update {
                            it.copy(
                                error = "Authentication error: $error",
                                navigationEvent = SplashNavigationEvent.NavigateToLogin
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error checking authentication: ${e.message}",
                        navigationEvent = SplashNavigationEvent.NavigateToLogin
                    )
                }
            }
        }
    }
}