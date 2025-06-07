package com.teksxt.closedtesting.domain.model

data class Notification(
    val id: String,
    val title: String,
    val body: String,
    val type: String,  // "chat_message", "reminder", "test_status", etc.
    val requestId: String,
    val dayNumber: String = "",
    val testerId: String = "",
    val channelId: String = "default",
    val createdAt: Long,
    val isRead: Boolean = false,
    val userId: String = ""
)