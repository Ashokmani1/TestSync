package com.teksxt.closedtesting.profile.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val userType: UserType = UserType.UNKNOWN,
    val isOnboarded: Boolean = false
)

enum class UserType {
    DEVELOPER,
    TESTER,
    UNKNOWN
}