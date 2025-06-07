package com.teksxt.closedtesting.chat.domain.repository

import android.net.Uri
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(message: ChatMessage): Result<String>

    suspend fun getMessagesForRequestAndUsers(requestId: String, userId1: String, userId2: String): Flow<Resource<List<ChatMessage>>>

    suspend fun sendImageMessage(requestId: String, senderId: String, receiverId: String, dayNumber: Int?, imageUri: Uri): Result<String>

    suspend fun markMessagesAsRead(messageIds: List<String>): Result<Unit>

    suspend fun sendReminderMessage(requestId: String, dayNumber: Int?, testerId: String): Result<String>

    suspend fun sendBulkReminders(requestId: String, dayNumber: Int?, testerIds: List<String>): Result<Int>

    suspend fun deleteMessage(messageId: String): Result<Unit>
}