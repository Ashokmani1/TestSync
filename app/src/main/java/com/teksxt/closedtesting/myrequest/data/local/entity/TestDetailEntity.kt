package com.teksxt.closedtesting.myrequest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "test_details")
data class TestDetailEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestId: String,
    val testerId: String,
    val testerName: String,
    val timestamp: String,
    val screenshotUrl: String?,
    val feedback: String?,
    val day: Int
)