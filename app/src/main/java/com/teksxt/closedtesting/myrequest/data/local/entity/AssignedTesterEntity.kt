package com.teksxt.closedtesting.myrequest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.model.TestingStatus

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

fun AssignedTesterEntity.toDomainModel(): AssignedTester {
    return AssignedTester(
        id = this.id.substringBefore('_'), // Extract testerId from the composite key
        name = this.name,
        email = this.email,
        hasCompleted = this.hasCompleted,
        lastActive = this.lastActive,
        avatarUrl = this.avatarUrl,
        feedback = this.feedback,
        screenshotUrl = this.screenshotUrl,
        dayNumber = this.dayNumber,
        testingStatus = TestingStatus.valueOf(this.testingStatus)
    )
}