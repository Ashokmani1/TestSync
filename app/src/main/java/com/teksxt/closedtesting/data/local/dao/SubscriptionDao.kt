package com.teksxt.closedtesting.data.local.dao

import androidx.room.*
import com.teksxt.closedtesting.data.local.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE userId = :userId")
    suspend fun deleteSubscriptionByUserId(userId: String)

    @Query("SELECT * FROM subscriptions WHERE userId = :userId")
    suspend fun getSubscriptionByUserId(userId: String): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE isActive = 1")
    fun getActiveSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE planType = :planType")
    fun getSubscriptionsByPlanType(planType: String): Flow<List<SubscriptionEntity>>

    @Query("UPDATE subscriptions SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateSubscription(userId: String)

    @Query("UPDATE subscriptions SET isActive = 1 WHERE userId = :userId")
    suspend fun activateSubscription(userId: String)
}