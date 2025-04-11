package com.teksxt.closedtesting.profile.domain.model

data class UserPreferences(
    val enableNotifications: Boolean = true,
    val enableEmailUpdates: Boolean = true
)