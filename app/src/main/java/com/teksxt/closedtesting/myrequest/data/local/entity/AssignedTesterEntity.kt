package com.teksxt.closedtesting.myrequest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assigned_testers")
data class AssignedTesterEntity(
    @PrimaryKey val id: String,
    val requestId: String,
    val dayNumber: Int,
    val name: String,
    val email: String,
    val hasCompleted: Boolean,
    val lastActive: String?,
    val avatarUrl: String?,
    val feedback: String?,
    val screenshotUrl: String?,
    val testingStatus: String
)