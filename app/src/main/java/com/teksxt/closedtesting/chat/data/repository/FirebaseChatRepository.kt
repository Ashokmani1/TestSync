package com.teksxt.closedtesting.chat.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.chat.domain.model.MessageType
import com.teksxt.closedtesting.chat.domain.repository.ChatRepository
import com.teksxt.closedtesting.core.util.Resource
import com.teksxt.closedtesting.domain.model.Notification
import com.teksxt.closedtesting.service.NotificationService
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class FirebaseChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) : ChatRepository {

    private val chatCollection = firestore.collection("chats")
    private val messagesCollection = firestore.collection("messages")

    override suspend fun sendMessage(message: ChatMessage): Result<String> {
        return try {
            // Create a unique chat ID if needed
            val chatId = getChatId(message.requestId, message.senderId, message.receiverId)

            // Ensure chat document exists
            ensureChatExists(chatId, message.requestId, message.senderId, message.receiverId)

            // Add message to messages collection
            val messageRef = messagesCollection.document()
            val messageWithId = message.copy(id = messageRef.id)

            messageRef.set(messageWithId).await()

            // Update chat document with last message
            updateChatWithLastMessage(chatId, messageWithId)

            sendMessageNotification(message)

            Result.success(messageRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // New method to handle message notifications
    private suspend fun sendMessageNotification(message: ChatMessage)
    {
        try {
            // Don't send notifications for own messages
            val currentUser = auth.currentUser
            if (currentUser?.uid == message.receiverId) return

            // Get sender information for the notification
            val senderUser = userRepository.getUserById(message.senderId).getOrNull()
            val senderName = senderUser?.name ?: "Someone"

            // Create notification content based on message type
            val (title, body) = when(message.messageType) {
                MessageType.TEXT -> {
                    val previewText = if (message.content.length > 30)
                        "${message.content.take(30)}..."
                    else
                        message.content
                    Pair("New message from $senderName", previewText)
                }
                MessageType.IMAGE -> {
                    Pair("New image from $senderName", "Tap to view the image")
                }
                MessageType.REMINDER -> {
                    Pair("Reminder", message.content)
                }
            }

            // Create notification data
            val notification = Notification(
                id = UUID.randomUUID().toString(),
                title = title,
                body = body,
                requestId = message.requestId,
                dayNumber = message.dayNumber?.toString() ?: "",
                type = if (message.messageType == MessageType.REMINDER) "reminder" else "chat_message",
                testerId = message.senderId,
                channelId = "chat_messages",
                createdAt = System.currentTimeMillis(),
                userId = message.receiverId
            )

            // Send notification to recipient
            notificationService.sendNotification(message.receiverId, notification)

        } catch (e: Exception) {
            // Log error but don't fail the message send operation
            println("Failed to send notification: ${e.message}")
        }
    }

    override suspend fun getMessagesForRequestAndUsers(
        requestId: String,
        userId1: String,
        userId2: String
    ): Flow<Resource<List<ChatMessage>>> = callbackFlow {
        trySend(Resource.Loading)

        val chatId = getChatId(requestId, userId1, userId2)

        val listener = messagesCollection
            .whereEqualTo("requestId", requestId)
            .whereIn("senderId", listOf(userId1, userId2))
            .whereIn("receiverId", listOf(userId1, userId2))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error loading messages"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)
                    }
                    trySend(Resource.Success(messages))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun sendImageMessage(requestId: String, senderId: String, receiverId: String, dayNumber: Int?, imageUri: Uri): Result<String>
    {
        return try {
            val imageName = "${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child("chat_images/$requestId/$imageName")

            // Upload image
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()

            // Create message
            val message = ChatMessage(
                requestId = requestId,
                senderId = senderId,
                receiverId = receiverId,
                dayNumber = dayNumber,
                imageUrl = downloadUrl,
                content = "",
                timestamp = System.currentTimeMillis(),
                messageType = MessageType.IMAGE
            )

            // Send message
            sendMessage(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMessagesAsRead(messageIds: List<String>): Result<Unit> {
        return try {
            val batch = firestore.batch()

            messageIds.forEach { messageId ->
                val messageRef = messagesCollection.document(messageId)
                batch.update(messageRef, "isRead", true)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendReminderMessage(
        requestId: String,
        dayNumber: Int?,
        testerId: String
    ): Result<String> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not authenticated")
            val currentUserData = userRepository.getUserById(currentUser.uid).getOrNull()
                ?: throw Exception("User data not found")

            val reminderText = "📢 ${currentUserData.name} sent a reminder for Day ${dayNumber ?: "all"}. Please update your test status."

            val message = ChatMessage(
                requestId = requestId,
                senderId = currentUser.uid,
                receiverId = testerId,
                dayNumber = dayNumber,
                content = reminderText,
                timestamp = System.currentTimeMillis(),
                messageType = MessageType.REMINDER
            )

            sendMessage(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper function to create a consistent chat ID
    private fun getChatId(requestId: String, userId1: String, userId2: String): String {
        val userIds = listOf(userId1, userId2).sorted().joinToString("_")
        return "${requestId}_${userIds}"
    }

    // Helper function to ensure chat document exists
    private suspend fun ensureChatExists(
        chatId: String,
        requestId: String,
        userId1: String,
        userId2: String
    ) {
        val chatRef = chatCollection.document(chatId)
        val chatDoc = chatRef.get().await()

        if (!chatDoc.exists()) {
            val chatData = hashMapOf(
                "id" to chatId,
                "requestId" to requestId,
                "participants" to listOf(userId1, userId2),
                "createdAt" to FieldValue.serverTimestamp(),
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "lastMessage" to null
            )

            chatRef.set(chatData).await()
        }
    }

    // Helper function to update chat with last message
    private suspend fun updateChatWithLastMessage(chatId: String, message: ChatMessage) {
        val chatRef = chatCollection.document(chatId)

        val updateData = hashMapOf<String, Any>(
            "lastMessage" to message,
            "lastMessageTimestamp" to message.timestamp
        )

        chatRef.update(updateData).await()
    }

    // Helper function to send bulk reminders to all testers
    override suspend fun sendBulkReminders(requestId: String, dayNumber: Int?, testerIds: List<String>): Result<Int>
    {
        return try {

            var successCount = 0

            testerIds.forEach { testerId ->
                val result = sendReminderMessage(requestId, dayNumber, testerId)
                if (result.isSuccess) {
                    successCount++
                }
            }

            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteMessage(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("ChatRepository", "Attempting to delete message with ID: $messageId")

            // Get the message first from messages collection
            val messageDoc = messagesCollection.document(messageId).get().await()
            if (!messageDoc.exists()) {
                return@withContext Result.failure(Exception("Message not found"))
            }

            // Extract data we need before deleting
            val message = messageDoc.toObject(ChatMessage::class.java)
            val requestId = message?.requestId
            val senderId = message?.senderId
            val receiverId = message?.receiverId
            val messageType = message?.messageType
            val imageUrl = message?.imageUrl

            if (requestId == null || senderId == null || receiverId == null) {
                return@withContext Result.failure(Exception("Message data incomplete"))
            }

            // Calculate the chat ID
            val chatId = getChatId(requestId, senderId, receiverId)

            if (messageType == MessageType.IMAGE && !imageUrl.isNullOrEmpty()) {
                try {
                    // Convert the download URL to a storage reference and delete the file
                    val storageRef = storage.getReferenceFromUrl(imageUrl)
                    storageRef.delete().await()
                    Log.d("ChatRepository", "Deleted image file from storage: $imageUrl")
                } catch (e: Exception) {
                    // Just log the error but continue with message deletion
                    Log.e("ChatRepository", "Failed to delete image: ${e.message}", e)
                }
            }

                // 1. Delete the message from messages collection
            messagesCollection.document(messageId).delete().await()
            Log.d("ChatRepository", "Message deleted from messages collection")


            // 2. Update chat metadata if needed
            val chatDoc = chatCollection.document(chatId).get().await()
            if (chatDoc.exists()) {
                val lastMessage = chatDoc.get("lastMessage") as? Map<*, *>
                val lastMessageId = lastMessage?.get("id") as? String

                // If this was the last message, update the last message
                if (lastMessageId == messageId) {
                    // Find new last message
                    val newLastMessageQuery = messagesCollection
                        .whereEqualTo("requestId", requestId)
                        .whereIn("senderId", listOf(senderId, receiverId))
                        .whereIn("receiverId", listOf(senderId, receiverId))
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()

                    if (!newLastMessageQuery.isEmpty) {
                        val newLastMessage = newLastMessageQuery.documents[0].toObject(ChatMessage::class.java)
                        chatCollection.document(chatId).update(
                            "lastMessage", newLastMessage,
                            "lastMessageTimestamp", newLastMessage?.timestamp
                        ).await()
                        Log.d("ChatRepository", "Updated chat with new last message")
                    } else {
                        // No messages left
                        chatCollection.document(chatId).update(
                            "lastMessage", null,
                            "lastMessageTimestamp", FieldValue.serverTimestamp()
                        ).await()
                        Log.d("ChatRepository", "No messages left, cleared last message")
                    }
                }

                // Remove from messageIds array if it exists
                if (chatDoc.contains("messageIds")) {
                    val messageIds = chatDoc.get("messageIds") as? List<String> ?: emptyList()
                    if (messageId in messageIds) {
                        val updatedMessageIds = messageIds.filter { it != messageId }
                        chatCollection.document(chatId).update(
                            "messageIds", updatedMessageIds
                        ).await()
                        Log.d("ChatRepository", "Removed message from messageIds array")
                    }
                }
            }

            Log.d("ChatRepository", "Successfully deleted message with ID: $messageId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error deleting message: ${e.message}", e)
            Result.failure(e)
        }
    }
}