package com.teksxt.closedtesting.settings.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.teksxt.closedtesting.settings.domain.model.Language
import com.teksxt.closedtesting.settings.domain.model.ThemeMode
import com.teksxt.closedtesting.settings.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
        val TEST_ASSIGNMENT_NOTIFICATIONS = booleanPreferencesKey("test_assignment_notifications")
        val FEEDBACK_NOTIFICATIONS = booleanPreferencesKey("feedback_notifications")
        val REMINDER_NOTIFICATIONS = booleanPreferencesKey("reminder_notifications")
        val SYSTEM_NOTIFICATIONS = booleanPreferencesKey("system_notifications")
        val FONT_SIZE_SCALE = floatPreferencesKey("font_size_scale")
        val CONTENT_DENSITY = intPreferencesKey("content_density")
        val LANGUAGE = stringPreferencesKey("language")
    }

    override suspend fun getThemeMode(): ThemeMode {
        val themeOrdinal = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.ordinal
        }.first()
        return ThemeMode.values()[themeOrdinal]
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.ordinal
        }
    }

    override suspend fun getTestAssignmentNotifications(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TEST_ASSIGNMENT_NOTIFICATIONS] ?: true
        }.first()
    }

    override suspend fun setTestAssignmentNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEST_ASSIGNMENT_NOTIFICATIONS] = enabled
        }
    }

    override suspend fun getFeedbackNotifications(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.FEEDBACK_NOTIFICATIONS] ?: true
        }.first()
    }

    override suspend fun setFeedbackNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FEEDBACK_NOTIFICATIONS] = enabled
        }
    }

    override suspend fun getReminderNotifications(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.REMINDER_NOTIFICATIONS] ?: true
        }.first()
    }

    override suspend fun setReminderNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_NOTIFICATIONS] = enabled
        }
    }

    override suspend fun getSystemNotifications(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SYSTEM_NOTIFICATIONS] ?: true
        }.first()
    }

    override suspend fun setSystemNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYSTEM_NOTIFICATIONS] = enabled
        }
    }

    override suspend fun getFontSizeScale(): Float {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.FONT_SIZE_SCALE] ?: 1.0f
        }.first()
    }

    override suspend fun setFontSizeScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE_SCALE] = scale
        }
    }

    override suspend fun getContentDensity(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CONTENT_DENSITY] ?: 1
        }.first()
    }

    override suspend fun setContentDensity(densityIndex: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTENT_DENSITY] = densityIndex
        }
    }

    override suspend fun getLanguage(): Language {
        val languageCode = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LANGUAGE] ?: Language.ENGLISH.code
        }.first()

        return Language.values().find { it.code == languageCode } ?: Language.ENGLISH
    }

    override suspend fun setLanguage(language: Language) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.code
        }
    }
}