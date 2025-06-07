package com.teksxt.closedtesting.notifications.domain.repository

import com.teksxt.closedtesting.core.util.Resource
import com.teksxt.closedtesting.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(forceRefresh: Boolean = false): Flow<Resource<List<Notification>>>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(notificationId: String)
    suspend fun clearAllNotifications()
    fun getUnreadCount(): Flow<Int>
    suspend fun syncNotifications()
}