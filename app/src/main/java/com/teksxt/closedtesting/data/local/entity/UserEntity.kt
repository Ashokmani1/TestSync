package com.teksxt.closedtesting.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.FieldValue
import com.teksxt.closedtesting.data.local.converter.DeviceListConverter
import com.teksxt.closedtesting.data.local.converter.NotificationPreferencesConverter
import com.teksxt.closedtesting.settings.domain.model.DeviceInfo
import com.teksxt.closedtesting.settings.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val emailVerified: Boolean,
    val createdAt: Long?,
    val lastActive: Long?,
    val accountStatus: String,
    val onboardingCompleted: Boolean,
    val fcmToken: String?,
    
    @TypeConverters(DeviceListConverter::class)
    val devices: List<DeviceInfo>?,
    
    // App owner fields
    val companyName: String?,
    val website: String?,
    val submittedApps: Int?,
    
    // Preferences
    @TypeConverters(NotificationPreferencesConverter::class)
    val notificationPreferences: Map<String, Boolean>?,
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val preferredLanguage: String,
    val appTheme: String,
    
    // Subscription info
    val subscriptionTier: String?,
    val subscriptionStatus: String?,
    val subscriptionExpiryDate: Long?,
    
    // Room-specific fields for sync
    val lastSyncedAt: Long,
    val isModifiedLocally: Boolean
) {
    companion object {

        fun fromDomainModel(user: User): UserEntity {

            return UserEntity(
                userId = user.id,
                email = user.email,
                displayName = user.name,
                fcmToken =  user.fcmToken,
                photoUrl = user.photoUrl,
                emailVerified = true, // Default to true or get from user model
                createdAt = user.createdAtMillis,
                lastActive = user.lastActiveMillis,
                accountStatus = "ACTIVE",
                onboardingCompleted = user.isOnboarded ?: false,
                devices = user.devices,
                companyName = user.companyName,
                website = user.website,
                submittedApps = user.submittedApps,
                notificationPreferences = user.notificationPreferences,
                emailNotifications = true, // Default or get from user model
                pushNotifications = true, // Default or get from user model
                preferredLanguage = "en", // Default or get from user model
                appTheme = "SYSTEM", // Default or get from user model
                subscriptionTier = user.subscriptionTier,
                subscriptionStatus = user.subscriptionStatus,
                subscriptionExpiryDate = user.subscriptionExpiryDate,
                lastSyncedAt = System.currentTimeMillis(),
                isModifiedLocally = false
            )
        }
    }

    fun toDomainModel(): User {
        return User(
            id = userId,
            email = email,
            name = displayName,
            photoUrl = photoUrl,
            isOnboarded = onboardingCompleted,
            devices = devices,
            fcmToken = fcmToken,
            companyName = companyName,
            website = website,
            submittedApps = submittedApps,
            notificationPreferences = notificationPreferences,
            subscriptionTier = subscriptionTier,
            subscriptionStatus = subscriptionStatus,
            subscriptionExpiryDate = subscriptionExpiryDate,
            createdAt = createdAt,
            lastActive = lastActive
        )
    }
}