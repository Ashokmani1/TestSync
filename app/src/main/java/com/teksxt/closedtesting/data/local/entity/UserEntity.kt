package com.teksxt.closedtesting.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val pushNotifications: Boolean,
    val appTheme: String,
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
                pushNotifications = true, // Default or get from user model
                appTheme = "SYSTEM", // Default or get from user model
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
            fcmToken = fcmToken,
            createdAt = createdAt,
            lastActive = lastActive
        )
    }
}