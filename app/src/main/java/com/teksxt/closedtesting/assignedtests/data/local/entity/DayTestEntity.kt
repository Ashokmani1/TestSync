package com.teksxt.closedtesting.assignedtests.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_tests")
data class DayTestEntity(
    @PrimaryKey val id: String,
    val testId: String,
    val day: Int,
    val isCompleted: Boolean = false,
    val screenshotUrl: String? = null,
    val feedback: String? = null,
    val completedAt: Long? = null,
    val lastUpdated: Long
)