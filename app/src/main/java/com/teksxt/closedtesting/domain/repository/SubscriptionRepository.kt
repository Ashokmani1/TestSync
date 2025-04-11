package com.teksxt.closedtesting.domain.repository

import com.teksxt.closedtesting.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    suspend fun purchaseSubscription(userId: String, planType: String, paymentMethod: String): Boolean
    
    fun getSubscriptionByUserId(userId: String): Flow<Subscription?>
    
    suspend fun updateSubscription(subscription: Subscription): Boolean
    
    suspend fun cancelSubscription(userId: String): Boolean
    
    suspend fun validateSubscription(userId: String): Boolean
}