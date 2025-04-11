package com.teksxt.closedtesting.service

import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.domain.model.NotificationData
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationService @Inject constructor(private val firestore: FirebaseFirestore)
{
    suspend fun sendNotification(
        userId: String,
        notification: NotificationData
    ) {
        val message = hashMapOf(
            "notification" to hashMapOf(
                "title" to notification.title,
                "body" to notification.body
            ),
            "data" to hashMapOf(
                "requestId" to notification.requestId,
                "dayNumber" to notification.dayNumber,
                "type" to notification.type
            ),
            "token" to getUserFCMToken(userId)
        )

        firestore.collection("notifications")
            .add(message)
            .await()
    }

    private suspend fun getUserFCMToken(userId: String): String {
        return firestore.collection("users")
            .document(userId)
            .get()
            .await()
            .getString("fcmToken") ?: throw Exception("FCM token not found")
    }
}