package com.teksxt.closedtesting.myrequest.data.local.dao

import androidx.room.*
import com.teksxt.closedtesting.myrequest.data.local.entity.TestDetailEntity

@Dao
interface TestDetailsDao {
    @Query("SELECT * FROM test_details WHERE requestId = :requestId")
    suspend fun getTestDetails(requestId: String): List<TestDetailEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestDetails(testDetails: List<TestDetailEntity>)

    @Query("SELECT * FROM test_details WHERE requestId = :requestId AND day = :day")
    suspend fun getTestDetailsForDay(requestId: String, day: Int): List<TestDetailEntity>
}