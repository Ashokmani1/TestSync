package com.teksxt.closedtesting.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.teksxt.closedtesting.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("UPDATE users SET onboardingCompleted = :isOnboarded WHERE userId = :userId")
    suspend fun updateUserOnboardingStatus(userId: String, isOnboarded: Boolean)

    @Query("UPDATE users SET lastSyncedAt = :syncTime, isModifiedLocally = :isModified WHERE userId = :userId")
    suspend fun updateSyncStatus(userId: String, syncTime: Long, isModified: Boolean)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
}