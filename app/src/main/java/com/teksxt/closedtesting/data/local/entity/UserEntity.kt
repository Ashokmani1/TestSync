package com.teksxt.closedtesting.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val role: String?, // "DEVELOPER" or "TESTER"
    val bio: String?,
    val skills: List<String>?,
    val createdAt: Date?,
    val lastLogin: Date?,
    val isOnboarded: Boolean = false
)