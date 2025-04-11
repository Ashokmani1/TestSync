package com.teksxt.closedtesting.myrequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.teksxt.closedtesting.myrequest.data.local.entity.AssignedTesterEntity

@Dao
interface AssignedTesterDao
{
    @Query("SELECT * FROM assigned_testers WHERE requestId = :requestId")
    suspend fun getAssignedTesters(requestId: String): List<AssignedTesterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignedTesters(testers: List<AssignedTesterEntity>)
}