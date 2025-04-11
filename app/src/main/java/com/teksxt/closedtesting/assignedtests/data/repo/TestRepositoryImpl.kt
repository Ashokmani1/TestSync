package com.teksxt.closedtesting.assignedtests.data.repo

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.assignedtests.data.local.dao.TestDao
import com.teksxt.closedtesting.assignedtests.data.local.entity.AssignedTestEntity
import com.teksxt.closedtesting.assignedtests.data.local.entity.DayTestEntity
import com.teksxt.closedtesting.assignedtests.domain.model.AssignedTest
import com.teksxt.closedtesting.assignedtests.domain.model.DayTest
import com.teksxt.closedtesting.assignedtests.domain.repo.TestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val testDao: TestDao
) : TestRepository
{

    private val TAG = "TestRepositoryImpl"

    override suspend fun getAssignedTests(): List<AssignedTest> = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

            // Fetch from Firestore
            val snapshot = firestore.collection("assigned_tests")
                .whereEqualTo("testerId", userId)
                .get()
                .await()

            val tests = snapshot.documents.mapNotNull { doc ->
                try {
                    val appSnapshot = firestore.collection("apps")
                        .document(doc.getString("appId") ?: "")
                        .get()
                        .await()

                    val entity = AssignedTestEntity(
                        id = doc.id,
                        appId = doc.getString("appId") ?: "",
                        appName = appSnapshot.getString("name") ?: "Unknown App",
                        description = appSnapshot.getString("description") ?: "",
                        status = doc.getString("status") ?: "PENDING",
                        testWindow = doc.getString("testWindow") ?: "",
                        completedDays = doc.getLong("completedDays")?.toInt() ?: 0,
                        totalDays = doc.getLong("totalDays")?.toInt() ?: 7,
                        todayStatus = doc.getString("todayStatus") ?: "Not started",
                        isTodayComplete = doc.getBoolean("isTodayComplete") ?: false,
                        playStoreLink = appSnapshot.getString("playStoreLink") ?: "",
                        groupLink = appSnapshot.getString("groupLink") ?: "",
                        lastUpdated = System.currentTimeMillis()
                    )

                    // Save to local DB
                    testDao.insertAssignedTest(entity)

                    entity.toDomainModel()
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing test document", e)
                    null
                }
            }

            if (tests.isNotEmpty()) {
                return@withContext tests
            }

            // If network fetch failed or returned empty, use cached data
            return@withContext testDao.getAllAssignedTests().map { it.toDomainModel() }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching assigned tests", e)
            // Return cached data on error
            return@withContext testDao.getAllAssignedTests().map { it.toDomainModel() }
        }
    }

    override suspend fun getTestById(testId: String): AssignedTest = withContext(Dispatchers.IO) {
        try {
            // Try to fetch from Firestore
            val doc = firestore.collection("assigned_tests")
                .document(testId)
                .get()
                .await()

            if (doc.exists()) {
                val appSnapshot = firestore.collection("apps")
                    .document(doc.getString("appId") ?: "")
                    .get()
                    .await()

                val entity = AssignedTestEntity(
                    id = doc.id,
                    appId = doc.getString("appId") ?: "",
                    appName = appSnapshot.getString("name") ?: "Unknown App",
                    description = appSnapshot.getString("description") ?: "",
                    status = doc.getString("status") ?: "PENDING",
                    testWindow = doc.getString("testWindow") ?: "",
                    completedDays = doc.getLong("completedDays")?.toInt() ?: 0,
                    totalDays = doc.getLong("totalDays")?.toInt() ?: 7,
                    todayStatus = doc.getString("todayStatus") ?: "Not started",
                    isTodayComplete = doc.getBoolean("isTodayComplete") ?: false,
                    playStoreLink = appSnapshot.getString("playStoreLink") ?: "",
                    groupLink = appSnapshot.getString("groupLink") ?: "",
                    lastUpdated = System.currentTimeMillis()
                )

                // Save to local DB
                testDao.insertAssignedTest(entity)

                return@withContext entity.toDomainModel()
            }

            // If not found in Firestore, try to get from local DB
            val cachedTest = testDao.getAssignedTestById(testId)
                ?: throw IllegalArgumentException("Test not found with ID: $testId")

            return@withContext cachedTest.toDomainModel()

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching test by ID", e)

            // Try to get from local DB
            val cachedTest = testDao.getAssignedTestById(testId)
                ?: throw IllegalArgumentException("Test not found with ID: $testId")

            return@withContext cachedTest.toDomainModel()
        }
    }

    override suspend fun getDayTests(testId: String): List<DayTest> = withContext(Dispatchers.IO) {
        try {
            // Fetch from Firestore
            val snapshot = firestore.collection("assigned_tests")
                .document(testId)
                .collection("day_tests")
                .get()
                .await()

            val dayTests = snapshot.documents.mapNotNull { doc ->
                try {
                    val day = doc.getLong("day")?.toInt() ?: return@mapNotNull null

                    val entity = DayTestEntity(
                        id = doc.id,
                        testId = testId,
                        day = day,
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        screenshotUrl = doc.getString("screenshotUrl"),
                        feedback = doc.getString("feedback"),
                        completedAt = doc.getLong("completedAt"),
                        lastUpdated = System.currentTimeMillis()
                    )

                    // Save to local DB
                    testDao.insertDayTest(entity)

                    entity.toDomainModel()
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing day test document", e)
                    null
                }
            }

            // If we have day tests, return them
            if (dayTests.isNotEmpty()) {
                return@withContext dayTests.sortedBy { it.day }
            }

            // If no day tests found in Firestore, try to get from local DB
            val cachedDayTests = testDao.getDayTestsByTestId(testId)

            // If we have cached day tests, return them
            if (cachedDayTests.isNotEmpty()) {
                return@withContext cachedDayTests.map { it.toDomainModel() }.sortedBy { it.day }
            }

            // If no day tests found in Firestore or local DB, create empty ones
            val test = getTestById(testId)
            return@withContext List(test.totalDays) { index ->
                DayTest(
                    day = index + 1,
                    isCompleted = index < test.completedDays,
                    screenshotUrl = null,
                    feedback = null,
                    completedAt = null
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching day tests", e)

            // Try to get from local DB
            val cachedDayTests = testDao.getDayTestsByTestId(testId)

            // If we have cached day tests, return them
            if (cachedDayTests.isNotEmpty()) {
                return@withContext cachedDayTests.map { it.toDomainModel() }.sortedBy { it.day }
            }

            // If no day tests found in local DB either, fall back to creating empty ones
            val test = getTestById(testId)
            return@withContext List(test.totalDays) { index ->
                DayTest(
                    day = index + 1,
                    isCompleted = index < test.completedDays,
                    screenshotUrl = null,
                    feedback = null,
                    completedAt = null
                )
            }
        }
    }

    override suspend fun uploadScreenshot(testId: String, day: Int, imageUri: String): Unit = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

            // Parse the URI
            val uri = Uri.parse(imageUri)

            // Upload to Firebase Storage
            val fileName = "test_${testId}_day_${day}_${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child("screenshots/$userId/$fileName")

            storageRef.putFile(uri).await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Update Firestore
            val dayTestRef = firestore.collection("assigned_tests")
                .document(testId)
                .collection("day_tests")
                .whereEqualTo("day", day)
                .get()
                .await()

            if (dayTestRef.documents.isNotEmpty()) {
                // Update existing day test
                val dayTestDoc = dayTestRef.documents[0]
                firestore.collection("assigned_tests")
                    .document(testId)
                    .collection("day_tests")
                    .document(dayTestDoc.id)
                    .update(
                        mapOf(
                            "screenshotUrl" to downloadUrl,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            } else {
                // Create new day test
                firestore.collection("assigned_tests")
                    .document(testId)
                    .collection("day_tests")
                    .add(
                        mapOf(
                            "day" to day,
                            "isCompleted" to false,
                            "screenshotUrl" to downloadUrl,
                            "createdAt" to System.currentTimeMillis(),
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }

            // Update local DB
            val dayTest = testDao.getDayTestByTestIdAndDay(testId, day)
            if (dayTest != null) {
                testDao.insertDayTest(dayTest.copy(
                    screenshotUrl = downloadUrl,
                    lastUpdated = System.currentTimeMillis()
                ))
            } else {
                testDao.insertDayTest(
                    DayTestEntity(
                        id = UUID.randomUUID().toString(),
                        testId = testId,
                        day = day,
                        isCompleted = false,
                        screenshotUrl = downloadUrl,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            // Check if both screenshot and feedback are present, if so mark as completed
            checkAndUpdateCompletion(testId, day)

        } catch (e: Exception) {
            Log.e(TAG, "Error uploading screenshot", e)
            throw e
        }
    }

    override suspend fun submitFeedback(testId: String, day: Int, feedback: String): Unit = withContext(Dispatchers.IO) {
        try {
            // Update Firestore
            val dayTestRef = firestore.collection("assigned_tests")
                .document(testId)
                .collection("day_tests")
                .whereEqualTo("day", day)
                .get()
                .await()

            if (dayTestRef.documents.isNotEmpty()) {
                // Update existing day test
                val dayTestDoc = dayTestRef.documents[0]
                firestore.collection("assigned_tests")
                    .document(testId)
                    .collection("day_tests")
                    .document(dayTestDoc.id)
                    .update(
                        mapOf(
                            "feedback" to feedback,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            } else {
                // Create new day test
                firestore.collection("assigned_tests")
                    .document(testId)
                    .collection("day_tests")
                    .add(
                        mapOf(
                            "day" to day,
                            "isCompleted" to false,
                            "feedback" to feedback,
                            "createdAt" to System.currentTimeMillis(),
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
            }

            // Update local DB
            val dayTest = testDao.getDayTestByTestIdAndDay(testId, day)
            if (dayTest != null) {
                testDao.insertDayTest(dayTest.copy(
                    feedback = feedback,
                    lastUpdated = System.currentTimeMillis()
                ))
            } else {
                testDao.insertDayTest(
                    DayTestEntity(
                        id = UUID.randomUUID().toString(),
                        testId = testId,
                        day = day,
                        isCompleted = false,
                        feedback = feedback,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            // Check if both screenshot and feedback are present, if so mark as completed
            checkAndUpdateCompletion(testId, day)

        } catch (e: Exception) {
            Log.e(TAG, "Error submitting feedback", e)
            throw e
        }
    }

    private suspend fun checkAndUpdateCompletion(testId: String, day: Int) {
        try {
            // Get the day test
            val dayTestRef = firestore.collection("assigned_tests")
                .document(testId)
                .collection("day_tests")
                .whereEqualTo("day", day)
                .get()
                .await()

            if (dayTestRef.documents.isNotEmpty()) {
                val dayTestDoc = dayTestRef.documents[0]
                val hasScreenshot = dayTestDoc.getString("screenshotUrl") != null
                val hasFeedback = dayTestDoc.getString("feedback") != null

                // If both screenshot and feedback are present, mark as completed
                if (hasScreenshot && hasFeedback) {
                    val now = System.currentTimeMillis()

                    // Update day test as completed
                    firestore.collection("assigned_tests")
                        .document(testId)
                        .collection("day_tests")
                        .document(dayTestDoc.id)
                        .update(
                            mapOf(
                                "isCompleted" to true,
                                "completedAt" to now
                            )
                        )
                        .await()

                    // Update local DB
                    val dayTest = testDao.getDayTestByTestIdAndDay(testId, day)
                    if (dayTest != null) {
                        testDao.insertDayTest(dayTest.copy(
                            isCompleted = true,
                            completedAt = now,
                            lastUpdated = now
                        ))
                    }

                    // Update the assigned test's completed days if this is a new completion
                    val test = firestore.collection("assigned_tests")
                        .document(testId)
                        .get()
                        .await()

                    val completedDays = test.getLong("completedDays")?.toInt() ?: 0
                    if (day > completedDays) {
                        // Update assigned test
                        firestore.collection("assigned_tests")
                            .document(testId)
                            .update(
                                mapOf(
                                    "completedDays" to day,
                                    "updatedAt" to now
                                )
                            )
                            .await()

                        // Update local DB
                        val testEntity = testDao.getAssignedTestById(testId)
                        if (testEntity != null) {
                            testDao.insertAssignedTest(testEntity.copy(
                                completedDays = day,
                                lastUpdated = now
                            ))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating completion status", e)
        }
    }

    private fun AssignedTestEntity.toDomainModel(): AssignedTest {
        return AssignedTest(
            id = id,
            appName = appName,
            description = description,
            status = status,
            testWindow = testWindow,
            completedDays = completedDays,
            totalDays = totalDays,
            progress = if (totalDays > 0) completedDays.toFloat() / totalDays else 0f,
            todayStatus = todayStatus,
            isTodayComplete = isTodayComplete,
            playStoreLink = playStoreLink,
            groupLink = groupLink
        )
    }

    private fun DayTestEntity.toDomainModel(): DayTest {
        return DayTest(
            day = day,
            isCompleted = isCompleted,
            screenshotUrl = screenshotUrl,
            feedback = feedback,
            completedAt = completedAt
        )
    }
}