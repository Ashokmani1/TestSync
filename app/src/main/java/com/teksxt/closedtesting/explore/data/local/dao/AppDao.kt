package com.teksxt.closedtesting.explore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.teksxt.closedtesting.explore.data.local.entity.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Update
    suspend fun updateApp(app: AppEntity)

    @Query("SELECT * FROM apps WHERE appId = :appId")
    suspend fun getAppById(appId: String): AppEntity?

    @Query("SELECT * FROM apps WHERE appId = :appId")
    fun getAppByIdFlow(appId: String): Flow<AppEntity?>

    @Query("SELECT * FROM apps WHERE ownerUserId = :userId")
    suspend fun getAppsByOwnerId(userId: String): List<AppEntity>

    @Query("SELECT * FROM apps WHERE ownerUserId = :userId")
    fun getAppsByOwnerIdFlow(userId: String): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE status IN (:statuses)")
    suspend fun getAppsByStatus(statuses: List<String>): List<AppEntity>

    @Query("SELECT * FROM apps")
    fun getAllAppsFlow(): Flow<List<AppEntity>>

    @Query("UPDATE apps SET lastSyncedAt = :syncTime, isModifiedLocally = :isModified WHERE appId = :appId")
    suspend fun updateSyncStatus(appId: String, syncTime: Long, isModified: Boolean)

    @Query("DELETE FROM apps WHERE appId = :appId")
    suspend fun deleteApp(appId: String)

    @Query("SELECT * FROM apps WHERE categoryId = :categoryId")
    suspend fun getAppsByCategory(categoryId: String): List<AppEntity>

    @Query("SELECT * FROM apps WHERE isModifiedLocally = 1")
    suspend fun getModifiedApps(): List<AppEntity>
}