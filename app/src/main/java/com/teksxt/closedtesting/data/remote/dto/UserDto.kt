package com.teksxt.closedtesting.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
import com.teksxt.closedtesting.settings.domain.model.DeviceInfo
import com.teksxt.closedtesting.settings.domain.model.User
import java.util.Date

data class UserDto(
    @PropertyName("userId")
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

    @PropertyName("pushNotifications")
    val pushNotifications: Boolean = true,

    @PropertyName("appTheme")
    val appTheme: String = "SYSTEM",

) {
    companion object {
        fun fromUser(user: User): UserDto {
            return UserDto(
                userId = user.id,
                email = user.email,
                displayName = user.name,
                photoUrl = user.photoUrl,
                fcmToken = user.fcmToken,
                emailVerified = false,
                createdAt = safelyConvertToTimestamp(user.createdAt),
                lastActive = safelyConvertToTimestamp(user.lastActive),
                accountStatus = "ACTIVE",
                onboardingCompleted = user.isOnboarded ?: false
            )
        }

        private fun safelyConvertToTimestamp(value: Any?): Timestamp? {
            if (value == null) return null

            return when (value) {
                is Timestamp -> value
                is Date -> Timestamp(value)
                is Long -> Timestamp(value / 1000, 0)
                is FieldValue -> null
                else -> try {
                    // Fallback: try to convert to Long if possible
                    Timestamp((value.toString().toLongOrNull() ?: System.currentTimeMillis()) / 1000, 0)
                } catch (e: Exception) {
                    // If all else fails, use current time
                    Timestamp.now()
                }
            }
        }
    }

    fun toUser(): User {
        return User(
            id = userId,
            email = email,
            name = displayName,
            photoUrl = photoUrl,
            isOnboarded = onboardingCompleted,
            fcmToken = fcmToken,
            createdAt = createdAt?.seconds?.times(1000),
            lastActive =  lastActive?.seconds?.times(1000)
        )
    }


}