package com.teksxt.closedtesting.assignedtests.domain.repo

import com.teksxt.closedtesting.assignedtests.domain.model.AssignedTest
import com.teksxt.closedtesting.assignedtests.domain.model.DayTest


interface TestRepository {
    suspend fun getAssignedTests(): List<AssignedTest>
    suspend fun getTestById(testId: String): AssignedTest
    suspend fun getDayTests(testId: String): List<DayTest>
    suspend fun uploadScreenshot(testId: String, day: Int, imageUri: String)
    suspend fun submitFeedback(testId: String, day: Int, feedback: String)
}