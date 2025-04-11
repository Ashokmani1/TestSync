package com.teksxt.closedtesting.data.remote.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class SubscriptionDto(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("planType") val planType: String = "", // FREE, BASIC, PREMIUM, etc.
    @PropertyName("startDate") val startDate: Date = Date(),
    @PropertyName("endDate") val endDate: Date = Date(),
    @PropertyName("isActive") val isActive: Boolean = false,
    @PropertyName("paymentMethod") val paymentMethod: String? = null,
    @PropertyName("transactionId") val transactionId: String? = null,
    @PropertyName("lastUpdated") val lastUpdated: Date = Date()
)