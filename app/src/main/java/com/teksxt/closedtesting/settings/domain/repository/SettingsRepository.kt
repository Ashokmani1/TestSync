package com.teksxt.closedtesting.settings.domain.repository

import com.teksxt.closedtesting.settings.domain.model.Language
import com.teksxt.closedtesting.settings.domain.model.ThemeMode

interface SettingsRepository {
    suspend fun getThemeMode(): ThemeMode
    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun getTestAssignmentNotifications(): Boolean
    suspend fun setTestAssignmentNotifications(enabled: Boolean)

    suspend fun getFeedbackNotifications(): Boolean
    suspend fun setFeedbackNotifications(enabled: Boolean)

    suspend fun getReminderNotifications(): Boolean
    suspend fun setReminderNotifications(enabled: Boolean)

    suspend fun getSystemNotifications(): Boolean
    suspend fun setSystemNotifications(enabled: Boolean)

    suspend fun getFontSizeScale(): Float
    suspend fun setFontSizeScale(scale: Float)

    suspend fun getContentDensity(): Int
    suspend fun setContentDensity(densityIndex: Int)

    suspend fun getLanguage(): Language
    suspend fun setLanguage(language: Language)
}