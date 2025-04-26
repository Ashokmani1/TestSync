package com.teksxt.closedtesting.myrequest.data.repo

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.teksxt.closedtesting.myrequest.data.local.dao.AssignedTesterDao
import com.teksxt.closedtesting.myrequest.data.local.dao.RequestDao
import com.teksxt.closedtesting.myrequest.data.local.entity.AssignedTesterEntity
import com.teksxt.closedtesting.myrequest.data.local.entity.RequestEntity
import com.teksxt.closedtesting.myrequest.data.remote.dto.RequestDto
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.domain.model.TestingStatus
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val requestDao: RequestDao,
    private val assignedTesterDao: AssignedTesterDao,
) : RequestRepository {

    private val requestsCollection = firestore.collection("requests")

    override suspend fun createRequest(request: Request): Result<Request> {
        return try {
            val newRequestId = request.id.ifEmpty { UUID.randomUUID().toString() }
            val requestWithId = request.copy(
                id = newRequestId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Create request in Firestore
            val requestDto = RequestDto.fromRequest(requestWithId)
            requestsCollection.document(newRequestId).set(requestDto).await()

            // Save to Room database
            val requestEntity = RequestEntity.fromDomainModel(requestWithId)
            requestDao.insertRequest(requestEntity)

            Result.success(requestWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRequest(request: Request): Result<Request> {
        return try {
            val updatedRequest = request.copy(updatedAt = System.currentTimeMillis())

            // Update request in Firestore
            val requestDto = RequestDto.fromRequest(updatedRequest)
            requestsCollection.document(request.id).set(requestDto, SetOptions.merge()).await()

            // Update in Room database
            val requestEntity = RequestEntity.fromDomainModel(updatedRequest)
            requestDao.updateRequest(requestEntity)

            Result.success(updatedRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRequestByAppID(appid: String): Result<Request?>
    {
        return try {
            // First try to get from local database
            val localRequest = requestDao.getRequestByAppId(appid)

            // Then fetch from Firestore and update local if needed
            val remoteDoc = requestsCollection.whereEqualTo("appId", appid).get().await()

            val remoteRequest = remoteDoc.documents.mapNotNull {
                it.toObject(RequestDto::class.java)?.toRequest()
            }.getOrNull(0)

            remoteRequest?.let { request ->

                // Update local database
                requestDao.insertRequest(RequestEntity.fromDomainModel(request))

                return Result.success(request)
            }

            // If Firestore fetch failed but we have local data, return that
            if (localRequest != null) {
                return Result.success(localRequest.toDomainModel())
            }

            Result.success(null)
        } catch (e: Exception) {
            // If we have local data and remote fetch failed, return local data
            try {
                val localRequest = requestDao.getRequestByAppId(appid)
                if (localRequest != null) {
                    return Result.success(localRequest.toDomainModel())
                }
            } catch (_: Exception) {}

            Result.failure(e)
        }
    }

    override suspend fun getUserRequests(): Flow<List<Request>> {
        val userId = auth.currentUser?.uid ?: return flow { emit(emptyList<Request>()) }

        return requestDao.getRequestsByOwnerIdFlow(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun deleteRequest(requestId: String): Result<Unit> {
        return try {
            // Delete from Firestore
            requestsCollection.document(requestId).delete().await()

            // Delete from local database
            requestDao.deleteRequest(requestId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getRequestByIdFlow(requestId: String): Flow<Resource<Request>> = flow {
        emit(Resource.Loading())

        try {
            // First emit from local database if available
            val localRequest = requestDao.getRequestById(requestId)
            if (localRequest != null) {
                emit(Resource.Success(localRequest.toDomainModel()))
            }

            // Then try to get fresh data from Firestore
            val remoteDoc = requestsCollection.document(requestId).get().await()
            if (remoteDoc.exists()) {
                val remoteRequest = remoteDoc.toObject(RequestDto::class.java)
                if (remoteRequest != null) {
                    val request = remoteRequest.toRequest()

                    // Update local database with fresh data
                    val requestEntity = RequestEntity.fromDomainModel(request).copy(
                        lastSyncedAt = System.currentTimeMillis(),
                        isModifiedLocally = false
                    )
                    requestDao.insertRequest(requestEntity)

                    emit(Resource.Success(request))
                }
            } else if (localRequest == null) {
                emit(Resource.Error("Request not found"))
            }
        } catch (e: Exception) {
            // If we previously emitted Success with local data, don't emit Error
            emit(Resource.Error("Failed to fetch request: ${e.message}"))
        }
    }

    override fun getUserRequestsFlow(): Flow<Resource<List<Request>>> = flow {
        emit(Resource.Loading())

        val userId = auth.currentUser?.uid ?: run {
            emit(Resource.Error("User not logged in"))
            return@flow
        }

        // First emit from local database
        try {
            requestDao.getRequestsByOwnerIdFlow(userId).collect { localRequests ->
                emit(Resource.Success(localRequests.map { it.toDomainModel() }))

                // Try to fetch from Firestore in background
                try {
                    val remoteQuery = requestsCollection.whereEqualTo("ownerUserId", userId).get().await()
                    val remoteRequests = remoteQuery.documents.mapNotNull {
                        it.toObject(RequestDto::class.java)?.toRequest()
                    }

                    // Save to local database
                    requestDao.insertRequests(remoteRequests.map { RequestEntity.fromDomainModel(it) })

                    // No need to emit again as the Room flow will emit the updated data
                } catch (e: Exception) {
                    // Silent fail as we already emitted local data
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load requests: ${e.message}"))
        }
    }

    override suspend fun syncRequestData() {
        try {
            // Push local modifications to Firestore
            val modifiedRequests = requestDao.getModifiedRequests()
            modifiedRequests.forEach { requestEntity ->
                val requestDto = RequestDto.fromRequest(requestEntity.toDomainModel())
                requestsCollection.document(requestEntity.requestId).set(requestDto, SetOptions.merge()).await()
                requestDao.updateSyncStatus(requestEntity.requestId, System.currentTimeMillis(), false)
            }

            // Pull all user requests from Firestore to keep local database updated
            val userId = auth.currentUser?.uid ?: return
            val remoteQuery = requestsCollection.whereEqualTo("ownerUserId", userId).get().await()
            val remoteRequests = remoteQuery.documents.mapNotNull {
                it.toObject(RequestDto::class.java)?.toRequest()
            }

            remoteRequests.forEach { request ->
                val localRequest = requestDao.getRequestById(request.id)
                if (localRequest == null || !localRequest.isModifiedLocally) {
                    requestDao.insertRequest(RequestEntity.fromDomainModel(request).copy(
                        lastSyncedAt = System.currentTimeMillis(),
                        isModifiedLocally = false
                    ))
                }
            }
        } catch (e: Exception) {
            // Log error or handle retry logic
        }
    }

    override suspend fun updateRequestStatus(requestId: String, status: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // Update status in Firestore
            requestsCollection.document(requestId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to Timestamp(now / 1000, 0)
                    )
                ).await()

            // Update local database
            requestDao.updateRequestStatus(requestId, status, now, now)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun togglePinRequest(requestId: String, isPinned: Boolean): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // Update pin status in Firestore
            requestsCollection.document(requestId)
                .update(
                    mapOf(
                        "pinned" to isPinned,
                        "updatedAt" to com.google.firebase.Timestamp(now / 1000, 0)
                    )
                ).await()

            // Update local database
            requestDao.updatePinnedStatus(requestId, isPinned, now, now)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun assignTester(requestId: String, testerId: String): Result<Unit>
    {
        return try {
            // First fetch the request to get testing days
            val requestDoc = requestsCollection.document(requestId).get().await()
            if (!requestDoc.exists()) {
                return Result.failure(Exception("Request not found"))
            }

            val requestDto = requestDoc.toObject(RequestDto::class.java)
            val testingDays = requestDto?.testingDays ?: 1

            // 1. Update tester list in the main request document
            requestsCollection.document(requestId).update(
                "testerIds", FieldValue.arrayUnion(testerId),
                "currentTestersCount", FieldValue.increment(1),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()

            // 2. Create tester document with day-by-day status tracking
            val statusByDay = (1..testingDays).associate { day ->
                day.toString() to TestingStatus.PENDING.name
            }

            val testerDoc = mapOf(
                "testerId" to testerId,
                "assignedAt" to FieldValue.serverTimestamp(),
                "statusByDay" to statusByDay,
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            firestore.collection("requests")
                .document(requestId)
                .collection("testers")
                .document(testerId)
                .set(testerDoc, SetOptions.merge())
                .await()

            // 3. Update local database for the request
            val localRequest = requestDao.getRequestById(requestId)
            if (localRequest != null) {
                val updatedTesterIds = localRequest.testerIds.orEmpty().toMutableList().apply {
                    if (!contains(testerId)) add(testerId)
                }

                val updatedRequest = localRequest.copy(
                    testerIds = updatedTesterIds,
                    currentTestersCount = localRequest.currentTestersCount + 1,
                    updatedAt = System.currentTimeMillis()
                )
                requestDao.updateRequest(updatedRequest)

                // 4. Create local assigned tester entities for each day
                val testerEntities = mutableListOf<AssignedTesterEntity>()

                // Get tester info if available
                val userData = try {
                    firestore.collection("users").document(testerId).get().await().data
                } catch (e: Exception) {
                    null
                }

                for (day in 1..testingDays) {
                    testerEntities.add(
                        AssignedTesterEntity(
                            id = "${testerId}_${day}",
                            requestId = requestId,
                            dayNumber = day,
                            name = userData?.get("displayName") as? String ?: "Unknown User",
                            email = userData?.get("email") as? String ?: "",
                            hasCompleted = false,
                            lastActive = "Just assigned",
                            avatarUrl = userData?.get("photoUrl") as? String,
                            feedback = null,
                            screenshotUrl = null,
                            testingStatus = TestingStatus.PENDING.name
                        )
                    )
                }

                // Insert all tester day entries
                if (testerEntities.isNotEmpty()) {
                    assignedTesterDao.insertAssignedTesters(testerEntities)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun assignUserToApp(appId: String)
    {
        try
        {
            val userId = auth.currentUser?.uid ?: return

            // Find active requests for this app
            val appRequests = requestsCollection
                .whereEqualTo("appId", appId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()

            if (!appRequests.isEmpty)
            {
                // Get the first active request for this app
                val requestId = appRequests.documents.first().id
                // Assign user as tester using requestRepository
                assignTester(requestId, userId)
            }
        } catch (e: Exception) {
            // Log error but don't fail the entire operation
            // The user is still "picked" even if assignment fails
        }
    }

    override suspend fun removeTester(requestId: String, testerId: String): Result<Unit> {
        return try {
            // Update tester list in Firestore
            requestsCollection.document(requestId).update(
                "testerIds", FieldValue.arrayRemove(testerId),
                "currentTestersCount", FieldValue.increment(-1)
            ).await()

            // Update local database
            val localRequest = requestDao.getRequestById(requestId)
            if (localRequest != null) {
                val updatedTesterIds = localRequest.testerIds.orEmpty().filter { it != testerId }

                val updatedRequest = localRequest.copy(
                    testerIds = updatedTesterIds,
                    currentTestersCount = (localRequest.currentTestersCount - 1).coerceAtLeast(0),
                    updatedAt = System.currentTimeMillis()
                )
                requestDao.updateRequest(updatedRequest)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    override suspend fun getAssignedTesters(requestId: String): Result<Map<Int, List<AssignedTester>>> {
        return try {
            //  Fetch the request document to get testerIds and test days
            val requestDoc = requestsCollection.document(requestId).get().await()
            if (!requestDoc.exists()) {
                return Result.failure(Exception("Request not found"))
            }

            val requestDto = requestDoc.toObject(RequestDto::class.java)
            val testerIds = requestDto?.testerIds ?: emptyList()
            val testingDays = requestDto?.testingDays ?: 1

            if (testerIds.isEmpty()) {
                return Result.success(emptyMap())
            }

            //  Prepare result containers
            val testersByDay = mutableMapOf<Int, MutableList<AssignedTester>>()
            val testerEntities = mutableListOf<AssignedTesterEntity>()

            // For each tester ID, fetch user data and tester status
            for (testerId in testerIds) {
                // Get user profile data
                val userDoc = firestore.collection("users").document(testerId).get().await()
                if (!userDoc.exists()) continue
                val userData = userDoc.data ?: continue

                // Get tester-specific data with status by day
                val testerDoc = firestore.collection("requests")
                    .document(requestId)
                    .collection("testers")
                    .document(testerId)
                    .get()
                    .await()

                // Extract the statusByDay map from tester document
                val statusByDay = if (testerDoc.exists()) {
                    testerDoc.get("statusByDay") as? Map<String, String> ?: emptyMap()
                } else {
                    emptyMap()
                }

                // For each tester, create entries for all testing days
                for (day in 1..testingDays) {
                    // Determine status based on statusByDay map
                    val statusString = statusByDay[day.toString()]
                    val status = when (statusString) {
                        TestingStatus.COMPLETED.name -> TestingStatus.COMPLETED
                        TestingStatus.IN_PROGRESS.name -> TestingStatus.IN_PROGRESS
                        else -> TestingStatus.PENDING
                    }

                    // Create a tester model with proper status
                    val tester = AssignedTester(
                        id = testerId,
                        name = userData["displayName"] as? String ?: "Unknown User",
                        email = userData["email"] as? String ?: "",
                        hasCompleted = status == TestingStatus.COMPLETED,
                        lastActive = formatTimestamp(userData["lastActive"] as? Timestamp),
                        avatarUrl = userData["photoUrl"] as? String,
                        feedback = null,
                        screenshotUrl = null,
                        dayNumber = day,
                        testingStatus = status
                    )

                    // Add to grouped results
                    if (!testersByDay.containsKey(day)) {
                        testersByDay[day] = mutableListOf()
                    }
                    testersByDay[day]?.add(tester)

                    // Also save to entity list for local storage
                    testerEntities.add(
                        AssignedTesterEntity(
                            id = "${testerId}_${day}",
                            requestId = requestId,
                            dayNumber = day,
                            name = tester.name,
                            email = tester.email,
                            hasCompleted = tester.hasCompleted,
                            lastActive = tester.lastActive,
                            avatarUrl = tester.avatarUrl,
                            feedback = tester.feedback,
                            screenshotUrl = tester.screenshotUrl,
                            testingStatus = tester.testingStatus.name
                        )
                    )
                }
            }


            // Step 6: Save to local database for future use
            if (testerEntities.isNotEmpty())
            {
                assignedTesterDao.insertAssignedTesters(testerEntities)
            }

            Result.success(testersByDay)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper function to format timestamp
    private fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return "N/A"

        val currentTime = System.currentTimeMillis()
        val timestampMillis = timestamp.seconds * 1000 + timestamp.nanoseconds / 1_000_000
        val diff = currentTime - timestampMillis

        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
        }
    }


    override suspend fun updateTesterDayStatus(
        requestId: String,
        testerId: String,
        dayNumber: Int,
        status: TestingStatus
    ): Result<Unit> {
        return try {
            // Update status in Firestore
            firestore.collection("requests")
                .document(requestId)
                .collection("testers")
                .document(testerId)
                .update(
                    "statusByDay.${dayNumber}", status.name,
                    "lastUpdated", FieldValue.serverTimestamp()
                ).await()

            // Update local database
            val entityId = "${testerId}_${dayNumber}"
            val entity = assignedTesterDao.getAssignedTesterById(entityId)
            if (entity != null) {
                assignedTesterDao.insertAssignedTesters(listOf(
                    entity.copy(
                        testingStatus = status.name,
                        hasCompleted = status == TestingStatus.COMPLETED
                    )
                ))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}