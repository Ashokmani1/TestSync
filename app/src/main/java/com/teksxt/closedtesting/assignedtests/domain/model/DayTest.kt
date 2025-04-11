package com.teksxt.closedtesting.assignedtests.domain.model

data class DayTest(
    val day: Int,
    val isCompleted: Boolean,
    val screenshotUrl: String? = null,
    val feedback: String? = null,
    val completedAt: Long? = null
)