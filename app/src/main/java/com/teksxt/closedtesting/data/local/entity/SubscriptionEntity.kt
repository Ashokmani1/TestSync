package com.teksxt.closedtesting.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val userId: String,
    val planType: String, // FREE, BASIC, PREMIUM, etc.
    val startDate: Date,
    val endDate: Date,
    val isActive: Boolean,
    val paymentMethod: String?,
    val transactionId: String?,
    val lastUpdated: Date
)