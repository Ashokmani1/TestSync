package com.teksxt.closedtesting.settings.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.settings.domain.model.User
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import com.teksxt.closedtesting.settings.domain.model.Language
import com.teksxt.closedtesting.settings.domain.model.ThemeMode
import com.teksxt.closedtesting.settings.domain.repository.SettingsRepository
import com.teksxt.closedtesting.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
        loadProfile()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val themeMode = settingsRepository.getThemeMode()
            val testAssignmentNotifications = settingsRepository.getTestAssignmentNotifications()
            val feedbackNotifications = settingsRepository.getFeedbackNotifications()
            val reminderNotifications = settingsRepository.getReminderNotifications()
            val systemNotifications = settingsRepository.getSystemNotifications()
            val fontSizeScale = settingsRepository.getFontSizeScale()
            val contentDensity = settingsRepository.getContentDensity()
            val language = settingsRepository.getLanguage()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    themeMode = themeMode,
                    testAssignmentNotifications = testAssignmentNotifications,
                    feedbackNotifications = feedbackNotifications,
                    reminderNotifications = reminderNotifications,
                    systemNotifications = systemNotifications,
                    fontSizeScale = fontSizeScale,
                    contentDensity = contentDensity,
                    language = language
                )
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(user = resource.data) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = resource.message ?: "Failed to load profile") }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    // Profile methods
    fun updateProfile(name: String, photoUri: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userRepository.updateUserProfile(name, photoUri)
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
            // Implement logout functionality
            // userRepository.logout()
        }
    }

    // Settings methods
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
            _uiState.update { it.copy(themeMode = themeMode) }
        }
    }

    fun toggleTestAssignmentNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTestAssignmentNotifications(enabled)
            _uiState.update { it.copy(testAssignmentNotifications = enabled) }
        }
    }

    fun toggleFeedbackNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFeedbackNotifications(enabled)
            _uiState.update { it.copy(feedbackNotifications = enabled) }
        }
    }

    fun toggleReminderNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReminderNotifications(enabled)
            _uiState.update { it.copy(reminderNotifications = enabled) }
        }
    }

    fun toggleSystemNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSystemNotifications(enabled)
            _uiState.update { it.copy(systemNotifications = enabled) }
        }
    }

    fun updateFontSize(scale: Float) {
        viewModelScope.launch {
            settingsRepository.setFontSizeScale(scale)
            _uiState.update { it.copy(fontSizeScale = scale) }
        }
    }

    fun updateContentDensity(densityIndex: Int) {
        viewModelScope.launch {
            settingsRepository.setContentDensity(densityIndex)
            _uiState.update { it.copy(contentDensity = densityIndex) }
        }
    }

    fun updateLanguage(language: Language) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
            _uiState.update { it.copy(language = language) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ProfileSettingsUiState(
    // Profile state
    val user: User? = null,

    // Settings state
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val testAssignmentNotifications: Boolean = true,
    val feedbackNotifications: Boolean = true,
    val reminderNotifications: Boolean = true,
    val systemNotifications: Boolean = true,
    val fontSizeScale: Float = 1.0f,
    val contentDensity: Int = 1, // 0: Compact, 1: Normal, 2: Comfortable
    val language: Language = Language.ENGLISH,

    // Common state
    val isLoading: Boolean = false,
    val error: String? = null
)