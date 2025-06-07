package com.teksxt.closedtesting.notifications.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.teksxt.closedtesting.domain.model.Notification

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val requestId: String,
    val dayNumber: String,
    val testerId: String,
    val channelId: String,
    val createdAt: Long,
    val isRead: Boolean,
    val userId: String
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
            createdAt = createdAt,
            isRead = isRead,
            userId = userId
        )
    }

    companion object {
        fun fromDomainModel(notification: Notification): NotificationEntity {
            return NotificationEntity(
                id = notification.id,
                title = notification.title,
                body = notification.body,
                type = notification.type,
                requestId = notification.requestId,
                dayNumber = notification.dayNumber,
                testerId = notification.testerId,
                channelId = notification.channelId,
                createdAt = notification.createdAt,
                isRead = notification.isRead,
                userId = ""
            )
        }
    }
}