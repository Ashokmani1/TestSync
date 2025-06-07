package com.teksxt.closedtesting.notifications.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.teksxt.closedtesting.domain.model.Notification

data class NotificationDto(
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("body")
    val body: String = "",

    @PropertyName("type")
    val type: String = "",

    @PropertyName("requestId")
    val requestId: String = "",

    @PropertyName("dayNumber")
    val dayNumber: String = "",

    @PropertyName("testerId")
    val testerId: String = "",

    @PropertyName("channelId")
    val channelId: String = "default",

    @PropertyName("createdAt")
    val createdAt: Timestamp? = null,

    @PropertyName("isRead")
    val isRead: Boolean = false,

    @PropertyName("userId")
    val userId: String = ""
) {
    fun toDomainModel(): Notification {
        return Notification(
            id = id,
            title = title,
            body = body,
            type = type,
            requestId = requestId,
            dayNumber = dayNumber,
            testerId = testerId,
            channelId = channelId,
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
            isRead = isRead
        )
    }

    companion object {
        fun fromDomainModel(notification: Notification, userId: String): NotificationDto {
            return NotificationDto(
                id = notification.id,
                title = notification.title,
                body = notification.body,
                type = notification.type,
                requestId = notification.requestId,
                dayNumber = notification.dayNumber,
                testerId = notification.testerId,
                channelId = notification.channelId,
                createdAt = Timestamp(notification.createdAt / 1000, 0),
                isRead = notification.isRead,
                userId = userId
            )
        }
    }
}