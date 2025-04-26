package com.teksxt.closedtesting.chat.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.chat.domain.model.MessageType
import com.teksxt.closedtesting.chat.domain.repository.ChatRepository
import com.teksxt.closedtesting.core.util.Resource
import com.teksxt.closedtesting.domain.model.NotificationData
import com.teksxt.closedtesting.service.NotificationService
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirebaseChatRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
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
                MessageType.FEEDBACK -> {
                    Pair("Feedback received", "$senderName left feedback on your test")
                }
                MessageType.SYSTEM -> {
                    Pair("System notification", message.content)
                }
            }

            // Create notification data
            val notification = NotificationData(
                title = title,
                body = body,
                requestId = message.requestId,
                dayNumber = message.dayNumber?.toString() ?: "",
                type = "chat_message",
                testerId = message.senderId,
                channelId = "chat_messages"
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

    override suspend fun sendImageMessage(
        requestId: String,
        senderId: String,
        receiverId: String,
        dayNumber: Int?,
        imageUri: Uri
    ): Result<String> {
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

    override suspend fun setUserTypingStatus(chatId: String, userId: String, isTyping: Boolean): Result<Unit> {
        return try {
            val chatRef = chatCollection.document(chatId)

            // Update typing status in chat metadata
            val typingField = "typingUsers.$userId"
            val updates = mapOf(
                typingField to if (isTyping) System.currentTimeMillis() else null
            )

            chatRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createFeedbackMessage(requestId: String, testerId: String, dayNumber: Int, content: String): Result<ChatMessage>
    {
        return try {
            val currentUser = firebaseAuth.currentUser ?: throw Exception("User not logged in")

            // Create a new message with FEEDBACK type
            val messageId = UUID.randomUUID().toString()
            val message = ChatMessage(
                id = messageId,
                requestId = requestId,
                senderId = currentUser.uid,
                receiverId = testerId,
                content = content,
                timestamp = System.currentTimeMillis(),
                dayNumber = dayNumber,
                messageType = MessageType.FEEDBACK,
                isCompleted = true // A feedback message implies day completion
            )

            sendMessage(message)

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}