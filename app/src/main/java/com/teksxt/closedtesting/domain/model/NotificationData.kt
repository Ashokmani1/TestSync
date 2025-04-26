package com.teksxt.closedtesting.domain.model

data class NotificationData(
    val title: String,
    val body: String,
    val requestId: String,
    val dayNumber: String = "",
    val type: String,
    val testerId: String = "",  // Add this for chat messages
    val channelId: String = "default"  // Add this for notification channels
)