package com.teksxt.closedtesting.settings.domain.model

import com.google.firebase.Timestamp


data class User(
    val id: String,
    val email: String,
    val name: String,
    val fcmToken: String? = null,
    val photoUrl: String? = null,
    val isOnboarded: Boolean? = false,
    val devices: List<DeviceInfo>? = null,
    val companyName: String? = null,
    val website: String? = null,
    val submittedApps: Int? = null,
    val notificationPreferences: Map<String, Boolean>? = null,
    val createdAt: Any? = null,
    val lastActive: Any? = null,
    val termsAccepted: Boolean = false
)
{
    // Helper properties to safely get timestamps as longs
    val createdAtMillis: Long?
        get() = when (createdAt) {
            is Long -> createdAt
            is Timestamp -> (createdAt as Timestamp).seconds * 1000 +
                    (createdAt as Timestamp).nanoseconds / 1000000
            else -> null
        }

    val lastActiveMillis: Long?
        get() = when (lastActive) {
            is Long -> lastActive
            is Timestamp -> (lastActive as Timestamp).seconds * 1000 +
                    (lastActive as Timestamp).nanoseconds / 1000000
            else -> null
        }
}