package com.teksxt.closedtesting.myrequest.domain.repo

import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.model.DayTestDetail
import com.teksxt.closedtesting.myrequest.domain.model.Request
import kotlinx.coroutines.flow.Flow

interface RequestRepository
{
    fun getAllRequests(): Flow<List<Request>>

    // Get requests created by the current user
    fun getUserRequests(): Flow<List<Request>>


    // Get a specific request by its ID
    fun getRequestById(requestId: String): Flow<Request?>

    // Sync requests from Firestore to Room
    suspend fun syncRequests()

    // Create a new request in both Firestore and Room
    suspend fun createRequest(request: Request): String

    // Update an existing request in both Firestore and Room
    suspend fun updateRequest(request: Request)

    // Delete a request from both Firestore and Room
    suspend fun deleteRequest(request: Request)

    // Clear cache (delete all local data)
    suspend fun clearCache()

    suspend fun getAssignedTesters(requestId: String): Map<Int, List<AssignedTester>>
    
    suspend fun sendReminder(
        requestId: String,
        dayNumber: Int?,
        testerId: String
    )
    
    suspend fun sendBulkReminders(
        requestId: String,
        dayNumber: Int?,
        testerIds: List<String>
    )

    suspend fun getTestDetails(requestId: String): Map<Int, List<DayTestDetail>>
}