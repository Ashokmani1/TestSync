package com.teksxt.closedtesting.assignedtests.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.teksxt.closedtesting.assignedtests.data.local.entity.AssignedTestEntity
import com.teksxt.closedtesting.assignedtests.data.local.entity.DayTestEntity

@Dao
interface TestDao {
    // Assigned Tests queries
    @Query("SELECT * FROM assigned_tests ORDER BY lastUpdated DESC")
    suspend fun getAllAssignedTests(): List<AssignedTestEntity>

    @Query("SELECT * FROM assigned_tests WHERE id = :testId LIMIT 1")
    suspend fun getAssignedTestById(testId: String): AssignedTestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignedTest(test: AssignedTestEntity)

    // Day Tests queries
    @Query("SELECT * FROM day_tests WHERE testId = :testId ORDER BY day ASC")
    suspend fun getDayTestsByTestId(testId: String): List<DayTestEntity>

    @Query("SELECT * FROM day_tests WHERE testId = :testId AND day = :day LIMIT 1")
    suspend fun getDayTestByTestIdAndDay(testId: String, day: Int): DayTestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayTest(dayTest: DayTestEntity)
}