package com.teksxt.closedtesting.notifications.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.teksxt.closedtesting.core.util.Resource
import com.teksxt.closedtesting.domain.model.Notification
import com.teksxt.closedtesting.notifications.data.local.dao.NotificationDao
import com.teksxt.closedtesting.notifications.data.local.entity.NotificationEntity
import com.teksxt.closedtesting.notifications.data.remote.dto.NotificationDto
import com.teksxt.closedtesting.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val notificationDao: NotificationDao
) : NotificationRepository
{

    private val notificationsCollection = firestore.collection("notifications")

    override fun getNotifications(forceRefresh: Boolean): Flow<Resource<List<Notification>>> = flow {
        emit(Resource.Loading)

        try
        { // Get the current user ID
            val userId = auth.currentUser?.uid
            if (userId == null)
            {
                emit(Resource.Error("User not authenticated"))
                return@flow
            }

            // First emit from local database
            val localNotifications = notificationDao.getAllNotifications()
            if (localNotifications.isNotEmpty() && !forceRefresh)
            {
                emit(Resource.Success(localNotifications.map { it.toDomainModel() }))
            }

            // Then get from Firestore
            val query = notificationsCollection.whereEqualTo("userId", userId).orderBy("createdAt", Query.Direction.DESCENDING).limit(100)

            val remoteNotifications = query.get().await().documents.mapNotNull { doc ->
                val dto = doc.toObject(NotificationDto::class.java)
                dto?.copy(id = doc.id)?.toDomainModel()
            }

            // Update local database
            val entities = remoteNotifications.map { notification ->
                NotificationEntity.fromDomainModel(notification).copy(userId = userId)
            }

            notificationDao.deleteAllNotifications()
            notificationDao.insertNotifications(entities)

            // Emit updated list
            emit(Resource.Success(remoteNotifications))

        }
        catch (e: Exception)
        { // If we already emitted local data, we don't want to emit an error
            // unless we were explicitly asked to refresh
            if (forceRefresh || notificationDao.getAllNotifications().isEmpty())
            {
                emit(Resource.Error("Failed to fetch notifications: ${e.message}"))
            }
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            // Update local database first for immediate UI feedback
            notificationDao.updateReadStatus(notificationId, true)

            // Then update Firestore
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()

        } catch (e: Exception) {
            // Handle the error but don't throw - we want the UI to appear responsive
            e.printStackTrace()
        }
    }

    override suspend fun markAllAsRead() {
        try {
            val userId = auth.currentUser?.uid ?: return

            // Update local database first
            notificationDao.markAllAsRead()

            // Get all user's unread notifications
            val batch = firestore.batch()
            val unreadDocs = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            // Batch update them all
            unreadDocs.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }

            batch.commit().await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteNotification(notificationId: String) {
        try {
            // Delete from local database first
            notificationDao.deleteNotification(notificationId)

            // Then delete from Firestore
            notificationsCollection.document(notificationId)
                .delete()
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun clearAllNotifications() {
        try {
            val userId = auth.currentUser?.uid ?: return

            // Clear local database first
            notificationDao.deleteAllNotifications()

            // Get all user's notifications
            val batch = firestore.batch()
            val allDocs = notificationsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Batch delete them all
            allDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()

        } catch (e: Exception) {
            throw e  // Propagate error to caller
        }
    }

    override fun getUnreadCount(): Flow<Int>
    {
        return notificationDao.getUnreadCount()
    }

    override suspend fun syncNotifications() {
        try {
            val userId = auth.currentUser?.uid ?: return

            // Get timestamp of most recent local notification
            val mostRecentLocalTime = notificationDao.getMostRecentTimestamp() ?: 0

            // Get only newer notifications from Firestore
            val query = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereGreaterThan("createdAt", mostRecentLocalTime)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            val newRemoteNotifications = query.get().await().documents.mapNotNull { doc ->
                val dto = doc.toObject(NotificationDto::class.java)
                dto?.copy(id = doc.id)?.toDomainModel()
            }

            // Add only the new ones to local database
            if (newRemoteNotifications.isNotEmpty()) {
                val entities = newRemoteNotifications.map { notification ->
                    NotificationEntity.fromDomainModel(notification).copy(userId = userId)
                }
                notificationDao.insertNotifications(entities)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper method to get real-time updates (optional implementation)
    fun getNotificationsRealtime(): Flow<Resource<List<Notification>>> = callbackFlow {
        trySend(Resource.Loading)

        val userId = auth.currentUser?.uid
        if (userId == null)
        {
            trySend(Resource.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        // Listen for real-time updates
        val listenerRegistration = notificationsCollection.whereEqualTo("userId", userId).orderBy("createdAt", Query.Direction.DESCENDING).limit(100).addSnapshotListener { snapshot, error ->
                if (error != null)
                {
                    trySend(Resource.Error("Failed to listen for notifications: ${error.message}"))
                    return@addSnapshotListener
                }

                if (snapshot != null)
                {
                    val notifications = snapshot.documents.mapNotNull { doc ->
                        val dto = doc.toObject(NotificationDto::class.java)
                        dto?.copy(id = doc.id)?.toDomainModel()
                    }

                    // Update local database
                    val entities = notifications.map { notification ->
                        NotificationEntity.fromDomainModel(notification)
                    }

                     CoroutineScope(Dispatchers.IO).launch {
                         notificationDao.deleteAllNotifications()
                         notificationDao.insertNotifications(entities)
                     }

                    trySend(Resource.Success(notifications))
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }
}