package com.teksxt.closedtesting.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.domain.model.BillingPeriod
import com.teksxt.closedtesting.domain.model.Subscription
import com.teksxt.closedtesting.domain.model.SubscriptionPlanType
import com.teksxt.closedtesting.domain.repository.SubscriptionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SubscriptionRepository {

    private val subscriptionsCollection = firestore.collection("subscriptions")

    override suspend fun purchaseSubscription(userId: String, planType: String, paymentMethod: String): Boolean {
        return try {
            // Parse plan type
            val subscriptionPlanType = try {
                SubscriptionPlanType.valueOf(planType)
            } catch (e: Exception) {
                SubscriptionPlanType.FREE
            }
            
            // Default to MONTHLY billing period
            val billingPeriod = BillingPeriod.MONTHLY
            
            // Calculate end date based on billing period
            val startDate = Date()
            val endDate = when (billingPeriod) {
                BillingPeriod.MONTHLY -> {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = startDate
                    calendar.add(java.util.Calendar.MONTH, 1)
                    calendar.time
                }
                BillingPeriod.YEARLY -> {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = startDate
                    calendar.add(java.util.Calendar.YEAR, 1)
                    calendar.time
                }
            }
            
            // First check if user already has an active subscription
            val existingSubscription = getSubscriptionByUserId(userId).collect { subscription ->
                if (subscription != null) {
                    // If there's an existing subscription, cancel it first
                    cancelSubscription(userId)
                }
            }
            
            // Create new subscription
            val subscriptionMap = hashMapOf(
                "userId" to userId,
                "planType" to subscriptionPlanType.name,
                "billingPeriod" to billingPeriod.name,
                "startDate" to Timestamp(startDate),
                "endDate" to Timestamp(endDate),
                "isActive" to true,
                "paymentMethod" to paymentMethod,
                "purchaseToken" to UUID.randomUUID().toString(),
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            
            val documentRef = subscriptionsCollection.document()
            documentRef.set(subscriptionMap).await()
            
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getSubscriptionByUserId(userId: String): Flow<Subscription?> = callbackFlow {
        val registration = subscriptionsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    val document = snapshot.documents[0]
                    val data = document.data
                    if (data != null) {
                        val subscription = (data["endDate"] as? Timestamp)?.toDate()?.let {
                            Subscription(
                                id = document.id, userId = data["userId"] as String, planType = try {
                                    SubscriptionPlanType.valueOf(data["planType"] as String)
                                } catch (e: Exception) {
                                    SubscriptionPlanType.FREE
                                }, billingPeriod = try {
                                    BillingPeriod.valueOf(data["billingPeriod"] as String)
                                } catch (e: Exception) {
                                    BillingPeriod.MONTHLY
                                }, startDate = (data["startDate"] as? Timestamp)?.toDate() ?: Date(), endDate = it, isActive = data["isActive"] as Boolean, paymentMethod = data["paymentMethod"] as? String ?: "", purchaseToken = data["purchaseToken"] as? String ?: "", createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(), updatedAt = (data["updatedAt"] as? Timestamp)?.toDate() ?: Date(), transactionId = data["transactionId"] as? String ?: "", lastUpdated = (data["lastUpdated"] as? Timestamp)?.toDate()
                            )
                        }
                        trySend(subscription).isSuccess
                    } else {
                        trySend(null).isSuccess
                    }
                } else {
                    trySend(null).isSuccess
                }
            }
            
        awaitClose { registration.remove() }
    }

    override suspend fun updateSubscription(subscription: Subscription): Boolean {
        return try {
            val subscriptionMap = hashMapOf(
                "userId" to subscription.userId,
                "planType" to subscription.planType.name,
                "billingPeriod" to subscription.billingPeriod.name,
                "startDate" to Timestamp(subscription.startDate),
                "endDate" to subscription.endDate?.let { Timestamp(it) },
                "isActive" to subscription.isActive,
                "paymentMethod" to subscription.paymentMethod,
                "purchaseToken" to subscription.purchaseToken,
                "updatedAt" to Timestamp.now(),
                "transactionId" to subscription.transactionId,
                "lastUpdated" to subscription.lastUpdated?.let { Timestamp(it) }
            )
            
            subscriptionsCollection.document(subscription.id).update(subscriptionMap as Map<String, Any>).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun cancelSubscription(userId: String): Boolean {
        return try {
            // Query to find the active subscription for this user
            val snapshot = subscriptionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()
                
            if (!snapshot.isEmpty) {
                val document = snapshot.documents[0]
                document.reference.update(
                    "isActive", false,
                    "updatedAt", Timestamp.now()
                ).await()
                true
            } else {
                // No active subscription found
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun validateSubscription(userId: String): Boolean {
        return try {
            // Query to find the active subscription for this user
            val snapshot = subscriptionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()
                
            if (!snapshot.isEmpty) {
                val document = snapshot.documents[0]
                val data = document.data
                
                if (data != null) {
                    val isActive = data["isActive"] as Boolean
                    val endDate = (data["endDate"] as? Timestamp)?.toDate()
                    
                    // Check if subscription is active and not expired
                    isActive && (endDate == null || endDate.after(Date()))
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}