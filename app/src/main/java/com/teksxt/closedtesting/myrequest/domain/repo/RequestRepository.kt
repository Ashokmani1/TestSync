package com.teksxt.closedtesting.myrequest.domain.repo

import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.domain.model.TestingStatus
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    suspend fun createRequest(request: Request): Result<Request>
    suspend fun updateRequest(request: Request): Result<Request>
    suspend fun getRequestByAppID(appid: String): Result<Request?>
    suspend fun getUserRequests(): Flow<List<Request>>
    suspend fun deleteRequest(requestId: String): Result<Unit>
    fun getRequestByIdFlow(requestId: String): Flow<Resource<Request>>
    fun getUserRequestsFlow(): Flow<Resource<List<Request>>>
    suspend fun syncRequestData()
    suspend fun updateRequestStatus(requestId: String, status: String): Result<Unit>
    suspend fun togglePinRequest(requestId: String, isPinned: Boolean): Result<Unit>
    suspend fun assignTester(requestId: String, testerId: String): Result<Unit>
    suspend fun removeTester(requestId: String, testerId: String): Result<Unit>
    suspend fun assignUserToApp(appId: String)
    suspend fun getAssignedTesters(requestId: String): Result<Map<Int, List<AssignedTester>>>
    suspend fun updateTesterDayStatus(requestId: String, testerId: String, dayNumber: Int, status: TestingStatus): Result<Unit>
    suspend fun getTesterDayStatus(requestId: String, testerId: String, dayNumber: Int): Result<TestingStatus>
}