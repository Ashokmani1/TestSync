package com.teksxt.closedtesting.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.teksxt.closedtesting.MainActivity
import com.teksxt.closedtesting.R

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        Log.d("FCMService", "FCMService created")
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "New token: $token")

        // Update the token in Firestore
        saveFCMTokenToFirestore(token)
    }

    override fun onMessageReceived(message: RemoteMessage)
    {
        super.onMessageReceived(message)
        Log.d("FCMService", "Message received: ${message.data}")

        try
        {
            // Extract message data
            val title = message.notification?.title ?: message.data["title"] ?: "New notification"
            val body = message.notification?.body ?: message.data["body"] ?: ""
            val requestId = message.data["requestId"] ?: ""
            val dayNumber = message.data["dayNumber"] ?: ""
            val type = message.data["type"] ?: "general"
            val testerId = message.data["testerId"] ?: ""
            val channelId = message.data["channelId"] ?: "default"

            // Create and show notification
            createNotificationChannel(channelId)
            showNotification(title, body, requestId, dayNumber, type, testerId, channelId)

            // Broadcast the notification for in-app handling if app is in foreground
            val intent = Intent("com.teksxt.closedtesting.NEW_NOTIFICATION")
            intent.putExtra("type", type)
            intent.putExtra("requestId", requestId)
            intent.putExtra("dayNumber", dayNumber)
            intent.putExtra("testerId", testerId)
            sendBroadcast(intent)

        }
        catch (e: Exception)
        {
            Log.e("FCMService", "Error processing notification: ${e.message}")
        }
    }

    private fun createNotificationChannel(channelId: String)
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Check if channel already exists
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel != null) return

            // Configure channel based on type
            val channelName = when (channelId) {
                "chat_messages" -> "Chat Messages"
                "reminders" -> "Test Reminders"
                "test_status" -> "Test Status Updates"
                else -> "Notifications"
            }

            val importance = when (channelId) {
                "chat_messages" -> NotificationManager.IMPORTANCE_HIGH
                "reminders" -> NotificationManager.IMPORTANCE_HIGH
                else -> NotificationManager.IMPORTANCE_DEFAULT
            }

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for $channelName"

                // Enable lights and vibration for chat and reminders
                if (channelId == "chat_messages" || channelId == "reminders") {
                    enableLights(true)
                    enableVibration(true)
                    lightColor = Color.BLUE
                }
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        requestId: String,
        dayNumber: String,
        type: String,
        testerId: String,
        channelId: String
    ) {
        val notificationId = when {
            // Use consistent IDs for chat notifications to update existing ones
            type == "chat_message" && testerId.isNotEmpty() -> testerId.hashCode()
            // Use request-specific IDs for request notifications
            requestId.isNotEmpty() -> requestId.hashCode()
            // Default random ID
            else -> System.currentTimeMillis().toInt()
        }

        // Create intent for when notification is tapped
        val intent = when (type) {
            "chat_message" -> {
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("navigate_to", "chat")
                    putExtra("requestId", requestId)
                    putExtra("testerId", testerId)
                    putExtra("dayNumber", dayNumber)
                }
            }
            "reminder" -> {
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("navigate_to", "request_details")
                    putExtra("requestId", requestId)
                    putExtra("dayNumber", dayNumber)
                }
            }
            else -> {
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            }
        }

        // Create pending intent for notification tap
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            this, notificationId, intent, pendingIntentFlag
        )

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification) // Make sure to create this icon
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        // Add style for longer messages
        if (body.length > 40) {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        // For chat messages, add 'Reply' action if Android version supports it
        if (type == "chat_message") {
            val replyLabel = "Reply"
            val remoteInput = RemoteInput.Builder("key_text_reply").setLabel(replyLabel).build()

            val replyIntent = Intent(this, ReplyReceiver::class.java).apply {
                putExtra("requestId", requestId)
                putExtra("testerId", testerId)
                putExtra("dayNumber", dayNumber)
            }

            val replyPendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val replyPendingIntent = PendingIntent.getBroadcast(
                this,
                notificationId + 1,
                replyIntent,
                replyPendingIntentFlag
            )

            val action = NotificationCompat.Action.Builder(
                R.drawable.ic_reply,
                replyLabel,
                replyPendingIntent
            ).addRemoteInput(remoteInput).build()

            notificationBuilder.addAction(action)
        }

        // Show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun saveFCMTokenToFirestore(token: String)
    {
        // Wait for auth to be ready
        auth.addAuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                serviceScope.launch {
                    try {
                        firestore.collection("users")
                            .document(userId)
                            .update(mapOf("fcmToken" to token))
                            .addOnSuccessListener {
                                Log.d("FCMService", "FCM token updated successfully for user $userId")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FCMService", "Failed to update FCM token: ${e.message}")
                            }
                    } catch (e: Exception) {
                        Log.e("FCMService", "Error updating FCM token: ${e.message}")
                    }
                }
            } else {
                Log.w("FCMService", "Cannot save FCM token - user not logged in")
            }
        }
    }
}