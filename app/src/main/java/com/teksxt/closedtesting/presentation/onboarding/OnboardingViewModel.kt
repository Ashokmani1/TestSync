package com.teksxt.closedtesting.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.data.preferences.UserPreferencesManager
import com.teksxt.closedtesting.presentation.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            sessionManager.completeOnboarding()
        }
    }
}