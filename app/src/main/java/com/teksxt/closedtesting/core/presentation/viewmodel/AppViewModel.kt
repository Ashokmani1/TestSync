package com.teksxt.closedtesting.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.settings.domain.model.ThemeMode
import com.teksxt.closedtesting.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application-level ViewModel that manages global app state and settings
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        // Load settings when ViewModel is created
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Collect theme mode changes
            settingsRepository.getThemeFlow().collect { themeMode ->
                _appState.update { it.copy(themeMode = themeMode) }
            }
        }



//        viewModelScope.launch {
//            // Collect any other app-wide settings
//            settingsRepository.getGeneralSettingsFlow().collect { generalSettings ->
//                _appState.update { it.copy(
//                    notificationsEnabled = generalSettings.notificationsEnabled,
//                    // Other app-wide settings
//                ) }
//            }
//        }
    }
}

/**
 * Application state data class holding all global app settings
 */
data class AppState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontSizeScale: Float = 1.0f,
    val notificationsEnabled: Boolean = true
)