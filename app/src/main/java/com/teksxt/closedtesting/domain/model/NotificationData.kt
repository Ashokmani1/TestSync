package com.teksxt.closedtesting.domain.model

data class NotificationData(
    val title: String,
    val body: String,
    val requestId: String,
    val dayNumber: Int?,
    val type: String = "REMINDER"
)