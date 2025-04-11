package com.teksxt.closedtesting.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.profile.domain.model.User
import com.teksxt.closedtesting.profile.domain.model.UserPreferences
import com.teksxt.closedtesting.profile.domain.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observePreferences()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val user = userRepository.getCurrentUser()
                _uiState.update { it.copy(user = user, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load profile: ${e.message}"
                    )
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userRepository.getUserPreferences()
                .collect { preferences ->
                    _uiState.update { it.copy(preferences = preferences) }
                }
        }
    }

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

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val updatedPrefs = uiState.value.preferences.copy(
                enableNotifications = enabled
            )

            userRepository.updateUserPreferences(updatedPrefs)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = "Failed to update preferences: ${error.message}")
                    }
                }
        }
    }

    fun toggleEmailUpdates(enabled: Boolean) {
        viewModelScope.launch {
            val updatedPrefs = uiState.value.preferences.copy(
                enableEmailUpdates = enabled
            )

            userRepository.updateUserPreferences(updatedPrefs)
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = "Failed to update preferences: ${error.message}")
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false,
    val error: String? = null
)