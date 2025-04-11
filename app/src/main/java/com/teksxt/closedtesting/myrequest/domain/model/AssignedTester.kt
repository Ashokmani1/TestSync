package com.teksxt.closedtesting.myrequest.domain.model

data class AssignedTester(
    val id: String,
    val name: String,
    val email: String,
    val hasCompleted: Boolean,
    val lastActive: String? = null,
    val avatarUrl: String? = null,
    val feedback: String? = null,
    val screenshotUrl: String? = null,
    val dayNumber: Int = 0,
    val testingStatus: TestingStatus = TestingStatus.PENDING
)

enum class TestingStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}