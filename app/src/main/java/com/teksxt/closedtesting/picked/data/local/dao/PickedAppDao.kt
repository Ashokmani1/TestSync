package com.teksxt.closedtesting.picked.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.teksxt.closedtesting.picked.data.local.entity.PickedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PickedAppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPickedApp(pickedApp: PickedAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPickedApps(pickedApps: List<PickedAppEntity>)

    @Update
    suspend fun updatePickedApp(pickedApp: PickedAppEntity)

    @Query("SELECT * FROM picked_apps WHERE id = :id")
    suspend fun getPickedAppById(id: String): PickedAppEntity?

    @Query("SELECT * FROM picked_apps WHERE userId = :userId AND appId = :appId")
    suspend fun getPickedAppByUserAndApp(userId: String, appId: String): PickedAppEntity?

    @Query("SELECT * FROM picked_apps WHERE userId = :userId ORDER BY isPinned DESC, pickedAt DESC")
    suspend fun getPickedAppsByUser(userId: String): List<PickedAppEntity>

    @Query("SELECT * FROM picked_apps WHERE userId = :userId ORDER BY isPinned DESC, pickedAt DESC")
    fun getPickedAppsByUserFlow(userId: String): Flow<List<PickedAppEntity>>

    @Query("SELECT * FROM picked_apps WHERE appId = :appId")
    suspend fun getPickedAppsByApp(appId: String): List<PickedAppEntity>

    @Query("SELECT * FROM picked_apps WHERE status = :status")
    suspend fun getPickedAppsByStatus(status: String): List<PickedAppEntity>

    @Query("UPDATE picked_apps SET status = :status, lastActivityAt = :lastActivityAt, isModifiedLocally = 1 WHERE id = :id")
    suspend fun updatePickedAppStatus(id: String, status: String, lastActivityAt: Long)

    @Query("UPDATE picked_apps SET completionRate = :completionRate, currentTestDay = :currentTestDay, lastActivityAt = :lastActivityAt, isModifiedLocally = 1 WHERE id = :id")
    suspend fun updatePickedAppProgress(id: String, completionRate: Float, currentTestDay: Int, lastActivityAt: Long)

    @Query("UPDATE picked_apps SET isPinned = :isPinned, isModifiedLocally = 1 WHERE id = :id")
    suspend fun updatePickedAppPin(id: String, isPinned: Boolean)

    @Query("UPDATE picked_apps SET lastSyncedAt = :syncTime, isModifiedLocally = :isModified WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncTime: Long, isModified: Boolean)

    @Query("DELETE FROM picked_apps WHERE id = :id")
    suspend fun deletePickedApp(id: String)

    @Query("SELECT * FROM picked_apps WHERE isModifiedLocally = 1")
    suspend fun getModifiedPickedApps(): List<PickedAppEntity>

    @Query("SELECT COUNT(*) FROM picked_apps WHERE appId = :appId")
    suspend fun getPickedAppCount(appId: String): Int
}