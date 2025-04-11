package com.teksxt.closedtesting.domain.usecase.subscription

import com.teksxt.closedtesting.domain.repository.SubscriptionRepository
import javax.inject.Inject

class PurchaseSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(userId: String, planType: String, paymentMethod: String): Boolean {
        return try {
            subscriptionRepository.purchaseSubscription(userId, planType, paymentMethod)
            true
        } catch (e: Exception) {
            false
        }
    }
}