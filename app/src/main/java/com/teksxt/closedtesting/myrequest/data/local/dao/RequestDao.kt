package com.teksxt.closedtesting.myrequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.teksxt.closedtesting.myrequest.data.local.entity.RequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: RequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<RequestEntity>)

    @Update
    suspend fun updateRequest(request: RequestEntity)

    @Query("SELECT * FROM requests WHERE requestId = :requestId")
    suspend fun getRequestById(requestId: String): RequestEntity?

    @Query("SELECT * FROM requests WHERE appId = :appId")
    suspend fun getRequestByAppId(appId: String): RequestEntity?

    @Query("SELECT * FROM requests WHERE requestId = :requestId")
    fun getRequestByIdFlow(requestId: String): Flow<RequestEntity?>

    @Query("SELECT * FROM requests WHERE ownerUserId = :userId ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getRequestsByOwnerId(userId: String): List<RequestEntity>

    @Query("SELECT * FROM requests WHERE ownerUserId = :userId ORDER BY isPinned DESC, updatedAt DESC")
    fun getRequestsByOwnerIdFlow(userId: String): Flow<List<RequestEntity>>

    @Query("SELECT * FROM requests WHERE status IN (:statuses) ORDER BY updatedAt DESC")
    suspend fun getRequestsByStatus(statuses: List<String>): List<RequestEntity>

    @Query("UPDATE requests SET status = :status, updatedAt = :updatedAt, lastSyncedAt = :lastSyncedAt, isModifiedLocally = 1 WHERE requestId = :requestId")
    suspend fun updateRequestStatus(requestId: String, status: String, updatedAt: Long, lastSyncedAt: Long)

    @Query("UPDATE requests SET isPinned = :isPinned, updatedAt = :updatedAt, lastSyncedAt = :lastSyncedAt, isModifiedLocally = 1 WHERE requestId = :requestId")
    suspend fun updatePinnedStatus(requestId: String, isPinned: Boolean, updatedAt: Long, lastSyncedAt: Long)

    @Query("UPDATE requests SET lastSyncedAt = :syncTime, isModifiedLocally = :isModified WHERE requestId = :requestId")
    suspend fun updateSyncStatus(requestId: String, syncTime: Long, isModified: Boolean)

    @Query("DELETE FROM requests WHERE requestId = :requestId")
    suspend fun deleteRequest(requestId: String)

    @Query("SELECT * FROM requests WHERE isModifiedLocally = 1")
    suspend fun getModifiedRequests(): List<RequestEntity>

    @Query("SELECT * FROM requests WHERE :currentTime BETWEEN startDate AND endDate")
    suspend fun getActiveRequests(currentTime: Long): List<RequestEntity>
}