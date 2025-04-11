package com.teksxt.closedtesting.explore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.teksxt.closedtesting.explore.data.local.entity.AppEntity
import com.teksxt.closedtesting.explore.data.local.entity.PickedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY lastUpdated DESC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT appId FROM picked_apps")
    fun getPickedApps(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPickedApp(pickedApp: PickedAppEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM picked_apps WHERE appId = :appId)")
    suspend fun isAppPicked(appId: String): Boolean

    @Query("SELECT * FROM apps WHERE id = :appId")
    fun getAppById(appId: String): Flow<AppEntity?>

    @Update
    suspend fun updateApp(app: AppEntity)
}