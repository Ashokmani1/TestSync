package com.teksxt.closedtesting.assignedtests.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assigned_tests")
data class AssignedTestEntity(
    @PrimaryKey val id: String,
    val appId: String,
    val appName: String,
    val description: String,
    val status: String,
    val testWindow: String,
    val completedDays: Int,
    val totalDays: Int,
    val todayStatus: String,
    val isTodayComplete: Boolean,
    val playStoreLink: String,
    val groupLink: String,
    val lastUpdated: Long
)