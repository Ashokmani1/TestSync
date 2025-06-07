package com.teksxt.closedtesting.service

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.domain.model.Notification
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationService @Inject constructor(private val firestore: FirebaseFirestore)
{
    /**
     * Send notification to a specific user
     */
    suspend fun sendNotification(userId: String, notification: Notification): Result<Unit> {
        return try {
            // Get the user's FCM token
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val token = userDoc.getString("fcmToken")
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("User has no FCM token registered"))
            }

            // Create notification document
            val notificationData = hashMapOf(
                "token" to token,
                "title" to notification.title,
                "body" to notification.body,
                "requestId" to notification.requestId,
                "dayNumber" to notification.dayNumber,
                "type" to notification.type,
                "testerId" to notification.testerId,
                "channelId" to notification.channelId,
                "userId" to notification.userId,
                "createdAt" to FieldValue.serverTimestamp(),
                "status" to "pending"
            )

            // Add to Firestore - this triggers the Cloud Function
            firestore.collection("notifications")
                .add(notificationData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationService", "Error sending notification: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun getUserFCMToken(userId: String): String {
        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        return userDoc.getString("fcmToken")
            ?: throw Exception("FCM token not found for user $userId")
    }

    suspend fun sendBulkNotifications(userIds: List<String>, notification: Notification): Result<Int>
    {
        var successCount = 0

        try {
            val batch = firestore.batch()

            for (userId in userIds) {
                try {
                    val token = getUserFCMToken(userId)

                    val message = hashMapOf(
                        // Same message structure as above
                        "token" to token
                    )

                    val docRef = firestore.collection("notifications").document()
                    batch.set(docRef, message)
                    successCount++
                } catch (e: Exception) {
                    // Log error but continue with other users
                    println("Failed to prepare notification for user $userId: ${e.message}")
                }
            }

            // Commit all notifications at once
            batch.commit().await()

            return Result.success(successCount)
        } catch (e: Exception) {
            return Result.failure(Exception("Failed to send bulk notifications: ${e.message}"))
        }
    }
}