package com.teksxt.closedtesting.myrequest.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.myrequest.data.local.dao.AssignedTesterDao
import com.teksxt.closedtesting.myrequest.data.local.dao.RequestDao
import com.teksxt.closedtesting.myrequest.data.local.dao.TestDetailsDao
import com.teksxt.closedtesting.explore.data.local.dao.AppDao
import com.teksxt.closedtesting.explore.data.local.entity.AppEntity
import com.teksxt.closedtesting.myrequest.data.local.entity.AssignedTesterEntity
import com.teksxt.closedtesting.myrequest.domain.helper.toDomainModel
import com.teksxt.closedtesting.myrequest.domain.helper.toEntity
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.model.DayTestDetail
import com.teksxt.closedtesting.domain.model.NotificationData
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.domain.model.TestingStatus
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.service.NotificationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RequestRepositoryImpl @Inject constructor(
    private val requestDao: RequestDao,
    private val assignedTesterDao: AssignedTesterDao,
    private val testDetailsDao: TestDetailsDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val notificationService: NotificationService,
    private val appDao: AppDao // Added appDao
): RequestRepository
{
    private val requestsCollection = firestore.collection("requests")
    private val appsCollection = firestore.collection("apps")

    // Get all requests from Room (local database)
    override fun getAllRequests(): Flow<List<Request>> {
        return requestDao.getAllRequests()
    }

    // Get requests created by the current user
    override fun getUserRequests(): Flow<List<Request>> {
        val userId = auth.currentUser?.uid ?: ""
        return requestDao.getRequestsByUser(userId)
    }

    // Get a specific request by its ID
    override fun getRequestById(requestId: String): Flow<Request?> {
        return requestDao.getRequestById(requestId)
    }

    // Sync requests from Firestore to Room
    override suspend fun syncRequests() {
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid

                // Query Firestore for requests
                // If userId is not null, get only user's requests, otherwise get all
                val query = if (userId != null) {
                    requestsCollection.whereEqualTo("createdBy", userId)
                } else {
                    requestsCollection
                }

                val snapshot = query.get().await()

                val requests = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Request::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }

                // Insert all fetched requests into Room database
                requestDao.insertRequests(requests)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    // Create a new request in both Firestore and Room
    override suspend fun createRequest(request: Request): String {
        return withContext(Dispatchers.IO) {
            try {
                // Generate appId from Play Store link if not provided
                val appId = if (request.appId.isBlank()) {
                    com.teksxt.closedtesting.util.TestSyncUtil.generateAppId(request.playStoreLink)
                } else {
                    request.appId
                }
                
                // Get reference for new request document
                val docRef = requestsCollection.document()
                val requestId = docRef.id
                
                // Create a new request with the generated Firestore ID and appId
                val requestWithId = request.copy(id = requestId, appId = appId)
                
                // Create document in Firestore with batch to update both request and app
                val batch = firestore.batch()

                // Set the request with the proper ID
                batch.set(docRef, requestWithId)

                // Update app in Firestore only if it exists
                val appRef = appsCollection.document(appId)
                // Check if app document exists before updating
                val appDoc = appRef.get().await()
                if (appDoc.exists()) {
                    batch.update(appRef, "currentTesters", FieldValue.increment(1))
                } else {
                    // Create the app document if it doesn't exist
                    val appData = hashMapOf(
                        "id" to appId,
                        "name" to requestWithId.appName,
                        "description" to requestWithId.description,
                        "playStoreLink" to requestWithId.playStoreLink,
                        "groupLink" to requestWithId.groupLink,
                        "requiredTesters" to requestWithId.numberOfTesters,
                        "currentTesters" to 1,
                        "createdAt" to System.currentTimeMillis(),
                        "createdBy" to (auth.currentUser?.uid ?: requestWithId.createdBy) // Add creator's user ID
                    )
                    batch.set(appRef, appData)
                }

                // Commit the batch
                batch.commit().await()

                // Insert the request with the Firestore ID into Room
                requestDao.insertRequest(requestWithId)

                // Update app in local database
                val appEntity = appDao.getAppById(appId).firstOrNull()
                if (appEntity != null) {
                    appDao.updateApp(appEntity.copy(
                        currentTesters = appEntity.currentTesters + 1
                    ))
                } else {
                    // Create new app entity if it doesn't exist locally
                    val newApp = AppEntity(
                        id = appId,
                        name = requestWithId.appName,
                        currentTesters = 1,
                        playStoreLink = request.playStoreLink,
                        description = request.description,
                        groupLink = request.groupLink,
                        requiredTesters = request.numberOfTesters,
                        testWindow = request.durationInDays.toString() + " days",
                        lastUpdated = System.currentTimeMillis(),
                        createdBy = auth.currentUser?.uid ?: requestWithId.createdBy // Add creator's user ID
                    )
                    appDao.insertApps(arrayListOf(newApp))
                }

                return@withContext requestId
            } catch (e: Exception) {
                throw e
            }
        }
    }

    // Update an existing request in both Firestore and Room
    override suspend fun updateRequest(request: Request) {
        withContext(Dispatchers.IO) {
            try {
                // Get old request from Room to check if app ID changed
                val oldRequest = requestDao.getRequestByIdSync(request.id)

                if (oldRequest != null && oldRequest.appId != request.appId) {
                    // App changed, update both old and new app counts
                    val batch = firestore.batch()

                    // Decrement count for old app if it exists
                    if (oldRequest.appId.isNotEmpty()) {
                        val oldAppRef = appsCollection.document(oldRequest.appId)
                        // Check if old app document exists before updating
                        val oldAppDoc = oldAppRef.get().await()
                        if (oldAppDoc.exists()) {
                            batch.update(oldAppRef, "currentTesters", FieldValue.increment(-1))
                        }
                    }

                    // Increment count for new app if it exists
                    if (request.appId.isNotEmpty()) {
                        val newAppRef = appsCollection.document(request.appId)
                        // Check if new app document exists before updating
                        val newAppDoc = newAppRef.get().await()
                        if (newAppDoc.exists()) {
                            batch.update(newAppRef, "currentTesters", FieldValue.increment(1))
                        } else {
                            // Create the app document if it doesn't exist
                            val appData = hashMapOf(
                                "id" to request.appId,
                                "currentTesters" to 1,
                                "createdAt" to System.currentTimeMillis()
                            )
                            batch.set(newAppRef, appData)
                        }
                    }

                    // Update request document
                    val requestRef = requestsCollection.document(request.id)
                    batch.set(requestRef, request)

                    // Commit the batch
                    batch.commit().await()

                    // Update apps in Room database
                    if (oldRequest.appId.isNotEmpty()) {
                        val oldAppEntity = appDao.getAppById(oldRequest.appId).firstOrNull()
                        if (oldAppEntity != null) {
                            appDao.updateApp(oldAppEntity.copy(
                                currentTesters = Math.max(0, oldAppEntity.currentTesters - 1)
                            ))
                        }
                    }

                    if (request.appId.isNotEmpty()) {
                        val newAppEntity = appDao.getAppById(request.appId).firstOrNull()
                        if (newAppEntity != null) {
                            appDao.updateApp(newAppEntity.copy(
                                currentTesters = newAppEntity.currentTesters + 1
                            ))
                        }
                    }
                } else {
                    // App didn't change, just update the request
                    requestsCollection.document(request.id).set(request).await()
                }

                // Update request in Room
                requestDao.updateRequest(request)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    // Delete a request from both Firestore and Room
    override suspend fun deleteRequest(request: Request) {
        withContext(Dispatchers.IO) {
            try {
                // Use batch to update both request and app
                val batch = firestore.batch()

                // Delete request document
                val requestRef = requestsCollection.document(request.id)
                batch.delete(requestRef)

                // Decrement app counter if app exists
                if (request.appId.isNotEmpty()) {
                    val appRef = appsCollection.document(request.appId)
                    // Check if app document exists before updating
                    val appDoc = appRef.get().await()
                    if (appDoc.exists()) {
                        batch.update(appRef, "currentTesters", FieldValue.increment(-1))
                    }
                }

                // Commit the batch
                batch.commit().await()

                // Delete from Room
                requestDao.deleteRequest(request)

                // Update app in Room database
                if (request.appId.isNotEmpty()) {
                    val appEntity = appDao.getAppById(request.appId).firstOrNull()
                    if (appEntity != null) {
                        appDao.updateApp(appEntity.copy(
                            currentTesters = Math.max(0, appEntity.currentTesters - 1)
                        ))
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    // Clear cache (delete all local data)
    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            try {
                requestDao.clearAllRequests()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun getAssignedTesters(requestId: String): Map<Int, List<AssignedTester>> {
        return withContext(Dispatchers.IO) {
            try {
                // Get testers from Firestore
                val testersSnapshot = requestsCollection
                    .document(requestId)
                    .collection("testers")
                    .get()
                    .await()

                // Convert to domain model and group by day
                val testers = testersSnapshot.documents.mapNotNull { doc ->
                    try {
                        AssignedTester(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            hasCompleted = doc.getBoolean("hasCompleted") ?: false,
                            lastActive = doc.getString("lastActive"),
                            avatarUrl = doc.getString("avatarUrl"),
                            feedback = doc.getString("feedback"),
                            screenshotUrl = doc.getString("screenshotUrl"),
                            dayNumber = doc.getLong("dayNumber")?.toInt() ?: 1,
                            testingStatus = doc.getString("testingStatus")?.let {
                                TestingStatus.valueOf(it)
                            } ?: TestingStatus.PENDING
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // Cache in Room database
                assignedTesterDao.insertAssignedTesters(testers.map { it.toEntity(requestId) })

                // Group by day number
                return@withContext testers.groupBy { it.dayNumber }
            } catch (e: Exception) {
                // Return cached data if available
                val cachedTesters = assignedTesterDao.getAssignedTesters(requestId)
                return@withContext cachedTesters.groupBy { it.dayNumber }
                    .mapValues { (_, testers) ->
                        testers.map { it.toDomainModel() }
                    }
            }
        }
    }

    override suspend fun sendReminder(
        requestId: String,
        dayNumber: Int?,
        testerId: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                // Create reminder document in Firestore
                val reminderData = hashMapOf(
                    "testerId" to testerId,
                    "dayNumber" to dayNumber,
                    "sentAt" to System.currentTimeMillis(),
                    "sentBy" to auth.currentUser?.uid
                )

                // Get request and tester details for notification
                val request = requestsCollection.document(requestId).get().await()
                val tester = requestsCollection
                    .document(requestId)
                    .collection("testers")
                    .document(testerId)
                    .get()
                    .await()

                val appName = request.getString("appName") ?: "the app"
                val testerName = tester.getString("name") ?: "Tester"

                // Send FCM notification
                notificationService.sendNotification(
                    userId = testerId,
                    notification = NotificationData(
                        title = "Testing Reminder",
                        body = "Hi $testerName, please complete your testing for $appName${dayNumber?.let { " Day $it" } ?: ""}",
                        requestId = requestId,
                        dayNumber = dayNumber
                    )
                )

                // Save reminder to Firestore
                requestsCollection
                    .document(requestId)
                    .collection("reminders")
                    .add(reminderData)
                    .await()

                // Update tester's lastReminder field
                requestsCollection
                    .document(requestId)
                    .collection("testers")
                    .document(testerId)
                    .update("lastReminderSent", System.currentTimeMillis())
                    .await()

            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun sendBulkReminders(
        requestId: String,
        dayNumber: Int?,
        testerIds: List<String>
    ) {
        withContext(Dispatchers.IO) {
            try {
                val batch = firestore.batch()
                val currentTime = System.currentTimeMillis()
                val request = requestsCollection.document(requestId).get().await()
                val appName = request.getString("appName") ?: "Test Sync"

                testerIds.forEach { testerId ->
                    // Get tester details
                    val tester = requestsCollection
                        .document(requestId)
                        .collection("testers")
                        .document(testerId)
                        .get()
                        .await()

                    val testerName = tester.getString("name") ?: "Tester"

                    // Send FCM notification
                    notificationService.sendNotification(
                        userId = testerId,
                        notification = NotificationData(
                            title = "Testing Reminder",
                            body = "Hi $testerName, please complete your testing for $appName${dayNumber?.let { " Day $it" } ?: ""}",
                            requestId = requestId,
                            dayNumber = dayNumber
                        )
                    )

                    // Add reminder document
                    val reminderRef = requestsCollection
                        .document(requestId)
                        .collection("reminders")
                        .document()

                    val reminderData = hashMapOf(
                        "testerId" to testerId,
                        "dayNumber" to dayNumber,
                        "sentAt" to currentTime,
                        "sentBy" to auth.currentUser?.uid
                    )

                    batch.set(reminderRef, reminderData)

                    // Update tester's lastReminder field
                    val testerRef = requestsCollection
                        .document(requestId)
                        .collection("testers")
                        .document(testerId)

                    batch.update(testerRef, "lastReminderSent", currentTime)
                }

                // Commit the batch
                batch.commit().await()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun getTestDetails(requestId: String): Map<Int, List<DayTestDetail>>
    {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch test details from Firestore
                val testDetailsSnapshot = requestsCollection
                    .document(requestId)
                    .collection("testDetails")
                    .get()
                    .await()

                // Parse the documents to domain model
                val testDetails = testDetailsSnapshot.documents.mapNotNull { doc ->
                    try {
                        DayTestDetail(
                            testerName = doc.getString("testerName") ?: "",
                            timestamp = doc.getString("timestamp") ?: "",
                            screenshotUrl = doc.getString("screenshotUrl"),
                            feedback = doc.getString("feedback"),
                            day = doc.getLong("dayNumber")?.toInt() ?: 0,
                            testerId = doc.getString("testerId") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // Cache in Room database
                testDetailsDao.insertTestDetails(testDetails.map { it.toEntity(requestId) })

                // Group by day number
                return@withContext testDetails.groupBy { it.day }

            } catch (e: Exception) {
                // Return cached data on error
                val cachedDetails = testDetailsDao.getTestDetails(requestId)
                return@withContext cachedDetails.groupBy { it.day }
                    .mapValues { (_, details) ->
                        details.map { it.toDomainModel() }
                    }
            }
        }
    }

    // Helper extension function to convert domain model to entity
    private fun AssignedTester.toEntity(requestId: String) = AssignedTesterEntity(
        id = id,
        requestId = requestId,
        name = name,
        email = email,
        hasCompleted = hasCompleted,
        lastActive = lastActive,
        avatarUrl = avatarUrl,
        feedback = feedback,
        screenshotUrl = screenshotUrl,
        dayNumber = dayNumber,
        testingStatus = testingStatus.name
    )

    private fun AssignedTesterEntity.toDomainModel() = AssignedTester(
        id = id,
        name = name,
        email = email,
        hasCompleted = hasCompleted,
        lastActive = lastActive,
        avatarUrl = avatarUrl,
        feedback = feedback,
        screenshotUrl = screenshotUrl,
        dayNumber = dayNumber,
        testingStatus = TestingStatus.valueOf(testingStatus)
    )
}