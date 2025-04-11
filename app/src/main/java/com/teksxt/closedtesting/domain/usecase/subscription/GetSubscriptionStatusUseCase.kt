package com.teksxt.closedtesting.domain.usecase.subscription

import com.teksxt.closedtesting.domain.model.Subscription
import com.teksxt.closedtesting.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class GetSubscriptionStatusUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(userId: String): Flow<Subscription?> {
        return subscriptionRepository.getSubscriptionByUserId(userId)
            .catch { emit(null) }
    }
}