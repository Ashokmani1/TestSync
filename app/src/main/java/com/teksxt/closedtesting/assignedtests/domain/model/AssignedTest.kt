package com.teksxt.closedtesting.assignedtests.domain.model

data class AssignedTest(
    val id: String,
    val appName: String,
    val description: String,
    val status: String,
    val testWindow: String,
    val completedDays: Int,
    val totalDays: Int,
    val progress: Float,
    val todayStatus: String,
    val isTodayComplete: Boolean,
    val playStoreLink: String,
    val groupLink: String
)