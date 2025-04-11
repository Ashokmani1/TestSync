package com.teksxt.closedtesting.domain.model

import java.util.Date

/**
 * Represents a user in the TestSync application.
 * This model contains all user-related information needed across the app.
 */
data class UserModel(
    // Core user identifiers
    val uid: String = "",
    val email: String = "",

    // Profile information
    val displayName: String? = null,
    val photoUrl: String? = null,
    val bio: String? = null,

    // Authentication status
    val isEmailVerified: Boolean = false,
    val registrationDate: Date? = null,
    val lastLoginDate: Date? = null,

    // Testing preferences & statistics
    val deviceInfo: DeviceInfo? = null,
    val testingExperience: TestingExperience = TestingExperience.BEGINNER,
    val testsCompleted: Int = 0,
    val testsCreated: Int = 0,
    val rating: Float = 0f,
    val testingStreak: Int = 0,

    // Preferences
    val notificationsEnabled: Boolean = true,
    val darkModePreference: DarkModePreference = DarkModePreference.SYSTEM,
    val language: String = "en",

    // Account status
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val isPremium: Boolean = false,
    val premiumExpiry: Date? = null
) {
    /**
     * Returns a shortened display name for UI purposes.
     * Uses the first name, or email prefix if name is not available.
     */
    val shortName: String
        get() = displayName?.split(" ")?.first()
            ?: email.substringBefore("@")

    /**
     * Gets the appropriate display name for UI.
     * Falls back to email prefix if no display name is set.
     */
    val displayNameOrEmail: String
        get() = displayName ?: email.substringBefore("@")

    /**
     * Returns initials for avatar placeholders.
     */
    val initials: String
        get() {
            if (displayName.isNullOrBlank()) {
                return email.take(1).uppercase()
            }

            return displayName.split(" ")
                .take(2)
                .joinToString("") { it.take(1).uppercase() }
        }

    /**
     * Checks if the user has premium features access.
     */
    fun hasPremiumAccess(): Boolean {
        if (!isPremium) return false

        return premiumExpiry?.after(Date()) ?: false
    }
}

/**
 * Represents user's device information for testing context.
 */
data class DeviceInfo(
    val deviceModel: String = "",
    val manufacturer: String = "",
    val osVersion: String = "",
    val screenSize: String = "",
    val screenResolution: String = ""
)

/**
 * Enum representing user's testing experience level.
 */
enum class TestingExperience {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT;

    fun toDisplayString(): String {
        return name.lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}

/**
 * Enum representing user's dark mode preference.
 */
enum class DarkModePreference {
    LIGHT,
    DARK,
    SYSTEM;

    fun toDisplayString(): String {
        return when(this) {
            LIGHT -> "Light"
            DARK -> "Dark"
            SYSTEM -> "System Default"
        }
    }
}

/**
 * Enum representing user account status.
 */
enum class AccountStatus {
    ACTIVE,
    SUSPENDED,
    DEACTIVATED,
    DELETED;

    fun isActive(): Boolean = this == ACTIVE
}