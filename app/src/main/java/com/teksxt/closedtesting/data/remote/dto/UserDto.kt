package com.teksxt.closedtesting.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
import com.teksxt.closedtesting.settings.domain.model.DeviceInfo
import com.teksxt.closedtesting.settings.domain.model.User
import java.util.Date

data class UserDto(
    @DocumentId
    val userId: String = "",

    @PropertyName("email")
    val email: String = "",

    @PropertyName("displayName")
    val displayName: String = "",

    @PropertyName("onboardingCompleted")
    val onboardingCompleted: Boolean = false,

    @PropertyName("photoUrl")
    val photoUrl: String? = null,

    @PropertyName("fcmToken")
    val fcmToken: String? = null,

    @PropertyName("emailVerified")
    val emailVerified: Boolean = false,

    @PropertyName("createdAt")
    val createdAt: Timestamp? =  null,

    @PropertyName("lastActive")
    val lastActive: Timestamp? = null,

    @PropertyName("accountStatus")
    val accountStatus: String = "ACTIVE",

    @PropertyName("devices")
    val devices: List<Map<String, String>>? = null,

    // App owner fields
    @PropertyName("companyName")
    val companyName: String? = null,

    @PropertyName("website")
    val website: String? = null,

    @PropertyName("submittedApps")
    val submittedApps: Int? = null,

    // Preferences
    @PropertyName("notificationPreferences")
    val notificationPreferences: Map<String, Boolean>? = null,

    @PropertyName("emailNotifications")
    val emailNotifications: Boolean = true,

    @PropertyName("pushNotifications")
    val pushNotifications: Boolean = true,

    @PropertyName("preferredLanguage")
    val preferredLanguage: String = "en",

    @PropertyName("appTheme")
    val appTheme: String = "SYSTEM",

    // Subscription info
    @PropertyName("subscriptionTier")
    val subscriptionTier: String? = null,

    @PropertyName("subscriptionStatus")
    val subscriptionStatus: String? = null,

    @PropertyName("subscriptionExpiryDate")
    val subscriptionExpiryDate: Timestamp? = null
) {
    companion object {
        fun fromUser(user: User): UserDto {
            return UserDto(
                userId = user.id,
                email = user.email,
                displayName = user.name,
                photoUrl = user.photoUrl,
                fcmToken = user.fcmToken,
                emailVerified = true, // Default value
                createdAt = user.createdAt?.let {
                    if (it is Timestamp) it 
                    else if (it is Date) Timestamp(it)
                    else Timestamp(it as Long / 1000, 0)
                },
                lastActive = user.lastActive?.let {
                    if (it is Timestamp) it 
                    else if (it is Date) Timestamp(it)
                    else Timestamp(it as Long / 1000, 0)
                },
                accountStatus = "ACTIVE",
                onboardingCompleted = user.isOnboarded ?: false,
                devices = user.devices?.map { device ->
                    mapOf(
                        "deviceName" to device.deviceName,
                        "platform" to device.platform,
                        "osVersion" to device.osVersion,
                        "screenSize" to device.screenSize
                    )
                },
                companyName = user.companyName,
                website = user.website,
                submittedApps = user.submittedApps,
                notificationPreferences = user.notificationPreferences,
                subscriptionTier = user.subscriptionTier,
                subscriptionStatus = user.subscriptionStatus,
                subscriptionExpiryDate = user.subscriptionExpiryDate?.let { Timestamp(it / 1000, 0) }
            )
        }
    }

    fun toUser(): User {
        return User(
            id = userId,
            email = email,
            name = displayName,
            photoUrl = photoUrl,
            isOnboarded = onboardingCompleted,
            devices = devices?.map { deviceMap ->
                DeviceInfo(
                    deviceName = deviceMap["deviceName"] ?: "",
                    platform = deviceMap["platform"] ?: "",
                    osVersion = deviceMap["osVersion"] ?: "",
                    screenSize = deviceMap["screenSize"] ?: ""
                )
            },
            fcmToken = fcmToken,
            companyName = companyName,
            website = website,
            submittedApps = submittedApps,
            notificationPreferences = notificationPreferences,
            subscriptionTier = subscriptionTier,
            subscriptionStatus = subscriptionStatus,
            subscriptionExpiryDate = subscriptionExpiryDate?.seconds?.times(1000),
            createdAt = createdAt,
            lastActive =  lastActive
        )
    }
}