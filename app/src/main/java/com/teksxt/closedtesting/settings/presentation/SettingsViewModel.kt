package com.teksxt.closedtesting.settings.presentation

import android.app.Application
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.core.util.FileUtil
import com.teksxt.closedtesting.domain.repository.AuthRepository
import com.teksxt.closedtesting.settings.domain.model.User
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import com.teksxt.closedtesting.settings.domain.model.Language
import com.teksxt.closedtesting.settings.domain.model.ThemeMode
import com.teksxt.closedtesting.settings.domain.repository.SettingsRepository
import com.teksxt.closedtesting.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<LogoutEvent>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    init {
        loadSettings()
        loadProfile()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getThemeFlow().collect { themeMode ->
                _uiState.update { it.copy(themeMode = themeMode) }
            }

            // Load push notification preference
            settingsRepository.getPushNotificationsEnabled().collect { enabled ->
                _uiState.update { it.copy(pushNotificationsEnabled = enabled) }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(user = resource.data, isLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = resource.message ?: "Failed to load profile", isLoading = false) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    // Profile methods
    fun updateProfile(name: String, photoUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val profileUri = FileUtil.compressImage(photoUri, application)

            userRepository.updateUserProfile(name, profileUri.toString())
                .onSuccess {
                    loadProfile()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to update profile: ${error.message}"
                        )
                    }
                }
        }
    }


    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                clearNotifications(application)

                // Call the repository's logout method
                val result = authRepository.logout()

                if (result.isSuccess) {
                    // Signal successful logout
                    _logoutEvent.emit(LogoutEvent.Success)
                } else {
                    // Handle error
                    val exception = result.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Logout failed: ${exception?.message ?: "Unknown error"}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Logout failed: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun clearNotifications(context: Context)
    {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    // Settings methods
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
            _uiState.update { it.copy(themeMode = themeMode) }
        }
    }

    fun togglePushNotifications(enabled: Boolean)
    {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Call the repository method instead of directly updating Firestore
                val result = userRepository.updatePushNotificationsPreference(enabled)

                if (result.isSuccess) {
                    // Also update the local settings for faster access in the app
                    settingsRepository.setPushNotificationsEnabled(enabled)

                    // Update UI state
                    _uiState.update {
                        it.copy(
                            pushNotificationsEnabled = enabled,
                            isLoading = false
                        )
                    }
                } else {
                    // Handle error
                    val exception = result.exceptionOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to update notification settings: ${exception?.message ?: "Unknown error"}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to update notification settings: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun rateApp(context: Context) {
        try {
            val packageName = context.packageName
            val uri = "market://details?id=$packageName".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Open Play Store in browser if app not available
            val uri = "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun inviteFriends(context: Context) {
        try {
            val inviteIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Collaborate on App Testing with TestSync")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey! I’m using TestSync to streamline app testing and feedback collection. " +
                            "Join me on TestSync and make testing more efficient: " +
                            "https://play.google.com/store/apps/details?id=${context.packageName}"
                )
            }
            context.startActivity(Intent.createChooser(inviteIntent, "Invite Friends to TestSync"))
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Unable to open the invite dialog") }
        }
    }

    fun shareToSocial(context: Context) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Managing app testing has never been easier. I’m using TestSync to organize test requests and collect real-time feedback. " +
                            "Check it out: https://testsync.app/download" // TODO check this.
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share TestSync with others"))
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Unable to open the share dialog") }
        }
    }
}

data class ProfileSettingsUiState(
    // Profile state
    val user: User? = null,

    // Settings state
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val pushNotificationsEnabled: Boolean = true,

    // Common state
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class LogoutEvent {
    object Success : LogoutEvent()
}