package com.teksxt.closedtesting.picked.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.teksxt.closedtesting.core.util.ProgressUtils
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.picked.data.local.dao.PickedAppDao
import com.teksxt.closedtesting.picked.data.local.entity.PickedAppEntity
import com.teksxt.closedtesting.picked.data.remote.dto.PickedAppDto
import com.teksxt.closedtesting.picked.domain.model.PickedApp
import com.teksxt.closedtesting.picked.domain.repo.PickedAppRepository
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PickedAppRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val pickedAppDao: PickedAppDao,
    private val appRepository: AppRepository,
    private val requestRepository: RequestRepository
) : PickedAppRepository {

    private val pickedAppsCollection = firestore.collection("user_picked_apps")

    override suspend fun pickApp(appId: String): Result<PickedApp> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            val now = System.currentTimeMillis()
            val id = "${userId}_${appId}"

            // Check if already picked
            val existingPick = pickedAppDao.getPickedAppById(id)
            if (existingPick != null) {
                return Result.success(existingPick.toDomainModel())
            }

            // Create new picked app
            val pickedApp = PickedApp(
                id = id,
                userId = userId,
                appId = appId,
                pickedAt = now,
                lastActivityAt = now
            )

            // Save to Firestore
            val pickedAppDto = PickedAppDto.fromPickedApp(pickedApp)
            pickedAppsCollection.document(id).set(pickedAppDto).await()

            // Save to local database
            val pickedAppEntity = PickedAppEntity.fromDomainModel(pickedApp).copy(
                lastSyncedAt = now,
                isModifiedLocally = false
            )

            pickedAppDao.insertPickedApp(pickedAppEntity)

            // Assign user as tester using requestRepository
            requestRepository.assignUserToApp(appId)

            // Update active tester count in app
            appRepository.incrementActiveTesters(appId)

            Result.success(pickedApp)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unpickApp(id: String): Result<Unit> {
        return try {
            // Delete from Firestore
            pickedAppsCollection.document(id).delete().await()

            // Delete from local database
            pickedAppDao.deletePickedApp(id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPickedAppById(id: String): Result<PickedApp?> {
        return try {
            // First try to get from local database
            val localPickedApp = pickedAppDao.getPickedAppById(id)

            if (localPickedApp != null) {
                // Get app details to know max testing days
                val appDetails = appRepository.getAppById(localPickedApp.appId).getOrNull()
                val maxTestDays = appDetails?.testingDays ?: 20 // Default max days

                // Calculate the current day based on elapsed time
                val updatedDay = calculateCurrentTestDay(localPickedApp, maxTestDays)

                // If day changed, update it in storage
                if (updatedDay != localPickedApp.currentTestDay) {
                    updatePickedAppProgress(
                        id = localPickedApp.id,
                        completionRate = localPickedApp.completionRate,
                        currentTestDay = updatedDay
                    )

                    // Return updated model
                    return Result.success(localPickedApp.copy(currentTestDay = updatedDay).toDomainModel())
                }

                return Result.success(localPickedApp.toDomainModel())
            }

            // Then fetch from Firestore and update local if needed
            val remoteDoc = pickedAppsCollection.document(id).get().await()

            if (remoteDoc.exists()) {
                val remotePickedApp = remoteDoc.toObject(PickedAppDto::class.java)
                remotePickedApp?.let {
                    // Convert to domain model
                    val pickedApp = it.toPickedApp()

                    // Update local database
                    pickedAppDao.insertPickedApp(PickedAppEntity.fromDomainModel(pickedApp).copy(
                        lastSyncedAt = System.currentTimeMillis(),
                        isModifiedLocally = false
                    ))

                    return Result.success(pickedApp)
                }
            }

            // If Firestore fetch failed but we have local data, return that
            if (localPickedApp != null) {
                return Result.success(localPickedApp.toDomainModel())
            }

            Result.success(null)
        } catch (e: Exception) {
            // If we have local data and remote fetch failed, return local data
            try {
                val localPickedApp = pickedAppDao.getPickedAppById(id)
                if (localPickedApp != null) {
                    return Result.success(localPickedApp.toDomainModel())
                }
            } catch (_: Exception) {}

            Result.failure(e)
        }
    }

    override suspend fun getUserPickedApps(): Result<List<PickedApp>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            // Try to get from Firestore
            val remoteQuery = pickedAppsCollection.whereEqualTo("userId", userId).get().await()
            val remotePickedApps = remoteQuery.documents.mapNotNull {
                it.toObject(PickedAppDto::class.java)?.toPickedApp()
            }

            // Save to local database
            pickedAppDao.insertPickedApps(remotePickedApps.map {
                PickedAppEntity.fromDomainModel(it).copy(
                    lastSyncedAt = System.currentTimeMillis(),
                    isModifiedLocally = false
                )
            })

            Result.success(remotePickedApps)
        } catch (e: Exception) {
            // If remote fetch fails, try to get from local database
            try {
                val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
                val localPickedApps = pickedAppDao.getPickedAppsByUser(userId)
                Result.success(localPickedApps.map { it.toDomainModel() })
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    override fun getUserPickedAppsFlow(): Flow<Resource<List<PickedApp>>> = flow {
        emit(Resource.Loading())

        val userId = auth.currentUser?.uid ?: run {
            emit(Resource.Error("User not logged in"))
            return@flow
        }

        try {
            // Observe local database
            pickedAppDao.getPickedAppsByUserFlow(userId).collect { localPickedApps ->

                emit(
                    Resource.Success(
                        localPickedApps.map { entity ->

                            val pickedApp = entity.toDomainModel()

                            val appDetails = runBlocking {
                                appRepository.getAppById(pickedApp.appId).getOrNull()
                            }
                            val maxTestDays = appDetails?.testingDays ?: 20 // Default max days

                            // Calculate current day based on elapsed time
                            val currentDay = calculateCurrentTestDay(entity, maxTestDays)

                            // Return updated model
                            pickedApp.copy(currentTestDay = currentDay)
                        }
                    )
                )

                // Try to fetch from Firestore in background
                try {
                    val remoteQuery = pickedAppsCollection.whereEqualTo("userId", userId).get().await()
                    val remotePickedApps = remoteQuery.documents.mapNotNull {
                        it.toObject(PickedAppDto::class.java)?.toPickedApp()
                    }

                    // Save to local database
                    pickedAppDao.insertPickedApps(remotePickedApps.map {
                        PickedAppEntity.fromDomainModel(it).copy(
                            lastSyncedAt = System.currentTimeMillis(),
                            isModifiedLocally = false
                        )
                    })

                    // No need to emit again as the Room flow will emit the updated data
                } catch (e: Exception) {
                    // Silent fail as we already emitted local data
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load picked apps: ${e.message}"))
        }
    }

    override suspend fun updatePickedAppStatus(id: String, status: String): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // Update in Firestore
            pickedAppsCollection.document(id)
                .update(
                    mapOf(
                        "status" to status,
                        "lastActivityAt" to com.google.firebase.Timestamp(now / 1000, 0)
                    )
                ).await()

            // Update in local database
            pickedAppDao.updatePickedAppStatus(id, status, now)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePickedAppProgress(id: String, completionRate: Float, currentTestDay: Int): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            // Update in Firestore
            pickedAppsCollection.document(id)
                .update(
                    mapOf(
                        "completionRate" to completionRate,
                        "currentTestDay" to currentTestDay,
                        "lastActivityAt" to com.google.firebase.Timestamp(now / 1000, 0)
                    )
                ).await()

            // Update in local database
            pickedAppDao.updatePickedAppProgress(id, completionRate, currentTestDay, now)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun togglePickedAppPin(id: String): Result<Unit> {
        return try {
            // Get current state
            val pickedApp = pickedAppDao.getPickedAppById(id)
                ?: return Result.failure(Exception("Picked app not found"))

            val newPinState = !pickedApp.isPinned

            // Update in Firestore
            pickedAppsCollection.document(id)
                .update("pinned", newPinState)
                .await()

            // Update in local database
            pickedAppDao.updatePickedAppPin(id, newPinState)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPickedAppData() {
        try {
            // Push local modifications to Firestore
            val modifiedPickedApps = pickedAppDao.getModifiedPickedApps()
            modifiedPickedApps.forEach { pickedAppEntity ->
                val pickedAppDto = PickedAppDto.fromPickedApp(pickedAppEntity.toDomainModel())
                pickedAppsCollection.document(pickedAppEntity.id).set(pickedAppDto, SetOptions.merge()).await()
                pickedAppDao.updateSyncStatus(pickedAppEntity.id, System.currentTimeMillis(), false)
            }

            // Pull user picked apps from Firestore to keep local database updated
            val userId = auth.currentUser?.uid ?: return
            val remoteQuery = pickedAppsCollection.whereEqualTo("userId", userId).get().await()
            val remotePickedApps = remoteQuery.documents.mapNotNull {
                it.toObject(PickedAppDto::class.java)?.toPickedApp()
            }

            remotePickedApps.forEach { pickedApp ->
                val localPickedApp = pickedAppDao.getPickedAppById(pickedApp.id)
                if (localPickedApp == null || !localPickedApp.isModifiedLocally) {
                    pickedAppDao.insertPickedApp(PickedAppEntity.fromDomainModel(pickedApp).copy(
                        lastSyncedAt = System.currentTimeMillis(),
                        isModifiedLocally = false
                    ))
                }
            }
        } catch (e: Exception) {
            // Log error or handle retry logic
        }
    }

    override suspend fun syncPickedAppById(id: String) {
        try {
            // Check if local picked app needs to be pushed to Firestore
            val localPickedApp = pickedAppDao.getPickedAppById(id)
            if (localPickedApp != null && localPickedApp.isModifiedLocally) {
                val pickedAppDto = PickedAppDto.fromPickedApp(localPickedApp.toDomainModel())
                pickedAppsCollection.document(id).set(pickedAppDto, SetOptions.merge()).await()
                pickedAppDao.updateSyncStatus(id, System.currentTimeMillis(), false)
            }

            // Get fresh data from Firestore
            val remoteDoc = pickedAppsCollection.document(id).get().await()
            if (remoteDoc.exists()) {
                val remotePickedApp = remoteDoc.toObject(PickedAppDto::class.java)
                if (remotePickedApp != null) {
                    val pickedApp = remotePickedApp.toPickedApp()

                    // Don't overwrite locally modified data
                    if (localPickedApp == null || !localPickedApp.isModifiedLocally) {
                        pickedAppDao.insertPickedApp(PickedAppEntity.fromDomainModel(pickedApp).copy(
                            lastSyncedAt = System.currentTimeMillis(),
                            isModifiedLocally = false
                        ))
                    }
                }
            }
        } catch (e: Exception) {
            // Log error or handle retry logic
        }
    }

    override suspend fun getPickedAppCount(appId: String): Result<Int> {
        return try {
            // Try to get from Firestore
            val remoteQuery = pickedAppsCollection.whereEqualTo("appId", appId).get().await()
            Result.success(remoteQuery.size())
        } catch (e: Exception) {
            // If remote fetch fails, try to get from local database
            try {
                val count = pickedAppDao.getPickedAppCount(appId)
                Result.success(count)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    override fun getPickedAppsFlow(): Flow<Resource<List<App>>> = flow {

        emit(Resource.Loading())

        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

            // Get picked app IDs from the user
            val pickedAppDocs = pickedAppsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val appIds = pickedAppDocs.documents.mapNotNull { doc ->
                doc.getString("appId")
            }

            if (appIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Fetch full app details for each ID
            val apps = appIds.mapNotNull { appId ->
                appRepository.getAppById(appId).getOrNull()
            }

            emit(Resource.Success(apps))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to load picked apps: ${e.message}"))
        }
    }

    private fun calculateCurrentTestDay(pickedApp: PickedAppEntity, maxDays: Int): Int
    {
        return ProgressUtils.calculateCurrentTestDay(pickedApp, maxDays)
    }
}