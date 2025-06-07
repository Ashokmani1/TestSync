package com.teksxt.closedtesting.settings.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.teksxt.closedtesting.settings.domain.model.Language
import com.teksxt.closedtesting.settings.domain.model.ThemeMode
import com.teksxt.closedtesting.settings.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val PUSH_NOTIFICATIONS_ENABLED = booleanPreferencesKey("push_notifications_enabled")
    }

    override suspend fun getThemeMode(): ThemeMode {
        val themeOrdinal = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.ordinal
        }.first()
        return ThemeMode.values()[themeOrdinal]
    }

    override fun getThemeFlow(): Flow<ThemeMode> {
        return context.dataStore.data.map { preferences ->
            val themeOrdinal = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.ordinal
            ThemeMode.values()[themeOrdinal]
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.ordinal
        }
    }

    override fun getPushNotificationsEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.PUSH_NOTIFICATIONS_ENABLED] ?: true // Default to enabled
        }
    }

    override suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PUSH_NOTIFICATIONS_ENABLED] = enabled
        }
    }
}