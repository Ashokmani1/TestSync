package com.teksxt.closedtesting.domain.usecase.subscription

import com.teksxt.closedtesting.domain.repository.SubscriptionRepository
import javax.inject.Inject

class ValidateSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(userId: String): Boolean {
        return try {
            subscriptionRepository.validateSubscription(userId)
        } catch (e: Exception) {
            false
        }
    }
}