package com.teksxt.closedtesting.chat.domain.model

data class ChatMessage(
    val id: String = "",
    val requestId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val dayNumber: Int? = null,
    val content: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0,
    val isRead: Boolean = false,
    val messageType: MessageType = MessageType.TEXT,
    val isCompleted: Boolean = false,
)

enum class MessageType {
    TEXT, IMAGE, REMINDER
}