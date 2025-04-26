package com.teksxt.closedtesting.myrequest.domain.model

data class Request(
    val id: String,
    val appId: String,
    val ownerUserId: String,
    val title: String,
    val description: String? = null,
    val status: String, // "ACTIVE", "COMPLETED"
    val requestType: String, // "FREE", "PREMIUM"
    val createdAt: Long,
    val updatedAt: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val testingDays: Int = 7,
    val requiredTestersCount: Int = 5,
    val currentTestersCount: Int? = 0,
    val testerIds: List<String>? = null,
    val isPublic: Boolean? = false,
    val completionRate: Float? = 0f,
    val isPinned: Boolean? = false,
    val currentDay: Int = 1,
    val completedDays: Set<Int> = emptySet()
)