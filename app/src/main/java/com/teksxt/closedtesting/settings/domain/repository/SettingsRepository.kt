package com.teksxt.closedtesting.settings.domain.repository

import com.teksxt.closedtesting.settings.domain.model.Language
import com.teksxt.closedtesting.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getThemeMode(): ThemeMode
    fun getThemeFlow(): Flow<ThemeMode>
    suspend fun setThemeMode(themeMode: ThemeMode)

    fun getPushNotificationsEnabled(): Flow<Boolean>
    suspend fun setPushNotificationsEnabled(enabled: Boolean)
}