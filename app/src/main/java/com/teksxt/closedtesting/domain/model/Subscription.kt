package com.teksxt.closedtesting.domain.model

import java.util.Date

data class Subscription(
    val id: String = "",
    val userId: String,
    val planType: SubscriptionPlanType = SubscriptionPlanType.FREE,
    val billingPeriod: BillingPeriod = BillingPeriod.MONTHLY,
    val startDate: Date,
    val endDate: Date? = null,
    val isActive: Boolean = false,
    val paymentMethod: String = "",
    val purchaseToken: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val transactionId: String = "",
    val lastUpdated: Date? = null
)