package com.teksxt.closedtesting.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.google.firebase.auth.FirebaseAuth
import com.teksxt.closedtesting.R
import com.teksxt.closedtesting.chat.data.repository.FirebaseChatRepository
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.chat.domain.model.MessageType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class ReplyReceiver : BroadcastReceiver() {

    @Inject
    lateinit var chatRepository: FirebaseChatRepository

    @Inject
    lateinit var auth: FirebaseAuth

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return
        }

        try {
            // Get the reply text from the notification
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            if (remoteInput == null) {
                Log.e("ReplyReceiver", "No reply text found")
                return
            }

            val replyText = remoteInput.getCharSequence("key_text_reply")?.toString() ?: ""
            if (replyText.isBlank()) {
                Log.e("ReplyReceiver", "Empty reply text")
                return
            }

            // Get conversation details from intent extras
            val requestId = intent.getStringExtra("requestId") ?: ""
            val testerId = intent.getStringExtra("testerId") ?: ""
            val dayNumber = intent.getStringExtra("dayNumber")?.toIntOrNull()

            // Create a notification that shows the reply is being sent
            showReplyingNotification(context, testerId.hashCode(), replyText)

            // Create and send the reply message
            scope.launch {
                try {
                    val currentUser = auth.uid
                    if (currentUser == null) {
                        Log.e("ReplyReceiver", "User not authenticated")
                        showErrorNotification(context, testerId.hashCode(), "Failed to send reply: Not logged in")
                        return@launch
                    }

                    val message = ChatMessage(
                        id = "",  // Will be set by repository
                        requestId = requestId,
                        senderId = currentUser,
                        receiverId = testerId,
                        content = replyText,
                        timestamp = System.currentTimeMillis(),
                        messageType = MessageType.TEXT,
                        dayNumber = dayNumber,
                        isRead = false
                    )

                    val result = chatRepository.sendMessage(message)

                    if (result.isSuccess) {
                        // Message sent successfully, update notification
                        showSentNotification(context, testerId.hashCode(), replyText)
                    } else {
                        // Error sending message
                        val error = result.exceptionOrNull()?.message ?: "Unknown error"
                        Log.e("ReplyReceiver", "Failed to send message: $error")
                        showErrorNotification(context, testerId.hashCode(), "Failed to send: $error")
                    }
                } catch (e: Exception) {
                    Log.e("ReplyReceiver", "Error sending reply: ${e.message}")
                    showErrorNotification(context, testerId.hashCode(), "Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ReplyReceiver", "Error handling reply: ${e.message}")
        }
    }

    // Show a notification indicating the reply is being sent
    private fun showReplyingNotification(context: Context, notificationId: Int, replyText: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "chat_messages")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Sending message...")
            .setContentText(replyText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    // Show a notification indicating the reply was sent successfully
    private fun showSentNotification(context: Context, notificationId: Int, replyText: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "chat_messages")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Message sent")
            .setContentText(replyText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setTimeoutAfter(3000) // Auto dismiss after 3 seconds
            .build()

        notificationManager.notify(notificationId, notification)
    }

    // Show a notification indicating an error occurred
    private fun showErrorNotification(context: Context, notificationId: Int, errorMessage: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "chat_messages")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Message not sent")
            .setContentText(errorMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}