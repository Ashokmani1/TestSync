package com.teksxt.closedtesting.picked.domain.model

data class PickedApp(
    val id: String, // Composite ID: userId_appId
    val userId: String,
    val appId: String,
    val pickedAt: Long,
    val lastActivityAt: Long? = null,
    val completionRate: Float = 0f,
    val currentTestDay: Int = 1,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, ABANDONED
    val isPinned: Boolean = false
)