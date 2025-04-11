package com.teksxt.closedtesting.domain.model

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "developer" or "tester"
    val bio: String = "",
    val experienceLevel: String = "", // "beginner", "intermediate", "expert"
    val profilePictureUrl: String? = null,
    val appLinks: List<String> = emptyList(), // For developers: links to published apps
    val deviceInfo: String = "", // For testers: device model, OS version
    val trustScore: Int = 0, // For testers: based on completed tests
    val completedTests: Int = 0, // For testers: number of tests completed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPremiumMember: Boolean = false
)