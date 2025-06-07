package com.teksxt.closedtesting.explore.data.repo

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.explore.data.local.dao.AppDao
import com.teksxt.closedtesting.explore.data.local.entity.AppEntity
import com.teksxt.closedtesting.explore.data.remote.dto.AppDto
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val appDao: AppDao
) : AppRepository {

    private val appsCollection = firestore.collection("apps")

    override suspend fun createApp(app: App): Result<App> {
        return try {
            val newAppId = app.id.ifEmpty { UUID.randomUUID().toString() }
            val appWithId = app.copy(id = newAppId)

            // Create app in Firestore
            val appDto = AppDto.fromApp(appWithId)
            appsCollection.document(newAppId).set(appDto).await()

            // Save to Room database
            val appEntity = AppEntity.fromDomainModel(appWithId)
            appDao.insertApp(appEntity)

            Result.success(appWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateApp(app: App): Result<App> {
        return try {
            // Update app in Firestore
            val appDto = AppDto.fromApp(app)
            appsCollection.document(app.id).set(appDto, SetOptions.merge()).await()

            // Update in Room database
            val appEntity = AppEntity.fromDomainModel(app)
            appDao.updateApp(appEntity)

            Result.success(app)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAppById(appId: String): Result<App?> {
        return try {
            // First try to get from local database
            val localApp = appDao.getAppById(appId)

            // Then fetch from Firestore and update local if needed
            val remoteDoc = appsCollection.document(appId).get().await()

            if (remoteDoc.exists()) {
                val remoteApp = remoteDoc.toObject(AppDto::class.java)
                remoteApp?.let {
                    // Convert to domain model
                    val app = it.toApp()

                    // Update local database
                    appDao.insertApp(AppEntity.fromDomainModel(app))

                    return Result.success(app)
                }
            }

            // If Firestore fetch failed but we have local data, return that
            if (localApp != null) {
                return Result.success(localApp.toDomainModel())
            }

            Result.success(null)
        } catch (e: Exception) {
            // If we have local data and remote fetch failed, return local data
            try {
                val localApp = appDao.getAppById(appId)
                if (localApp != null) {
                    return Result.success(localApp.toDomainModel())
                }
            } catch (_: Exception) {}

            Result.failure(e)
        }
    }

    override suspend fun getAppsByOwner(userId: String): Result<List<App>> {
        return try {
            // Try to get from Firestore
            val remoteApps: List<App> = appsCollection.whereEqualTo("owner_user_id", userId).get().await()
                .documents.mapNotNull { it.toObject(AppDto::class.java)?.toApp() }

            appDao.insertApps(remoteApps.map { AppEntity.fromDomainModel(it) })

            Result.success<List<App>>(remoteApps)
        } catch (e: Exception) {
            // If remote fetch fails, try to get from local database
            try {
                val localApps: List<App> = appDao.getAppsByOwnerId(userId).map { it.toDomainModel() }
                Result.success<List<App>>(localApps)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    override suspend fun getAppsByStatus(statuses: List<String>): Result<List<App>> {
        return try {
            // Try to get from local database first (for quick UI response)
            val localApps= appDao.getAppsByStatus(statuses)

            // Try to fetch from Firestore (in background)
            try {
                val remoteQuery = appsCollection.whereIn("status", statuses).get().await()
                val remoteApps : List<App>  = remoteQuery.documents.mapNotNull {
                    it.toObject(AppDto::class.java)?.toApp()
                }

                // Save to local database
                appDao.insertApps(remoteApps.map { AppEntity.fromDomainModel(it) })

                Result.success<List<App>>(remoteApps)
            } catch (e: Exception) {
                // If Firestore fetch fails, return local data
                Result.success<List<App>>(localApps.map { it.toDomainModel() })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllApps(): Result<List<App>> {
        return try {
            // Try to fetch from Firestore
            val remoteQuery = appsCollection.get().await()
            val remoteApps : List<App>  = remoteQuery.documents.mapNotNull {
                it.toObject(AppDto::class.java)?.toApp()
            }

            // Save to local database
            appDao.insertApps(remoteApps.map { AppEntity.fromDomainModel(it) })

            Result.success<List<App>>(remoteApps)
        } catch (e: Exception) {
            // If Firestore fetch fails, try to get from local database
            try {
                val localAppsFlow = appDao.getAllAppsFlow()
                val localApps = localAppsFlow.first()
                Result.success<List<App>>(localApps.map { it.toDomainModel() })
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    override suspend fun deleteApp(appId: String): Result<Unit> {
        return try {
            // Delete from Firestore
            appsCollection.document(appId).delete().await()

            // Delete from local database
            appDao.deleteApp(appId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAppByIdFlow(appId: String): Flow<Resource<App>> = flow {
        emit(Resource.Loading())

        try {
            // First emit from local database if available
            val localApp = appDao.getAppById(appId)
            if (localApp != null) {
                emit(Resource.Success(localApp.toDomainModel()))
            }

            // Then try to get fresh data from Firestore
            val remoteDoc = appsCollection.document(appId).get().await()
            if (remoteDoc.exists()) {
                val remoteApp = remoteDoc.toObject(AppDto::class.java)
                if (remoteApp != null) {
                    val app = remoteApp.toApp()

                    // Update local database with fresh data
                    val appEntity = AppEntity.fromDomainModel(app).copy(
                        lastSyncedAt = System.currentTimeMillis(),
                        isModifiedLocally = false
                    )
                    appDao.insertApp(appEntity)

                    emit(Resource.Success(app))
                }
            } else if (localApp == null) {
                emit(Resource.Error("App not found"))
            }
        } catch (e: Exception) {
            // If we previously emitted Success with local data, don't emit Error
            emit(Resource.Error("Failed to fetch app: ${e.message}"))
        }
    }

    override fun getAppsByOwnerFlow(userId: String): Flow<Resource<List<App>>> = flow {
        emit(Resource.Loading())

        // First emit from local database
        try {
            val localApps = appDao.getAppsByOwnerId(userId)
            emit(Resource.Success(localApps.map { it.toDomainModel() }))
        } catch (e: Exception) {
            // If local fetch fails, continue to remote fetch
        }

        // Then try to get fresh data from Firestore
        try {
            val remoteQuery = appsCollection.whereEqualTo("owner_user_id", userId).get().await()
            val remoteApps: List<App> = remoteQuery.documents.mapNotNull {
                it.toObject(AppDto::class.java)?.toApp()
            }

            // Save to local database
            appDao.insertApps(remoteApps.map { AppEntity.fromDomainModel(it) })

            emit(Resource.Success(remoteApps))
        } catch (e: Exception) {
            // If we already emitted local data, don't emit Error
            emit(Resource.Error("Failed to fetch apps: ${e.message}"))
        }
    }

    override fun getAllAppsFlow(): Flow<Resource<List<App>>> = flow {
        emit(Resource.Loading())

        // First emit from local database
        try {
            appDao.getAllAppsFlow().collect { localApps ->
                emit(Resource.Success(localApps.map { it.toDomainModel() }))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Failed to fetch local apps: ${e.message}"))
        }
    }

    override suspend fun syncAppData() {
        try {
            // Push local modifications to Firestore
            val modifiedApps = appDao.getModifiedApps()
            modifiedApps.forEach { appEntity ->
                val appDto = AppDto.fromApp(appEntity.toDomainModel())
                appsCollection.document(appEntity.appId).set(appDto, SetOptions.merge()).await()
                appDao.updateSyncStatus(appEntity.appId, System.currentTimeMillis(), false)
            }

            // Pull all apps from Firestore to keep local database updated
            val remoteQuery = appsCollection.get().await()
            val remoteApps : List<App> = remoteQuery.documents.mapNotNull {
                it.toObject(AppDto::class.java)?.toApp()
            }

            remoteApps.forEach { app ->
                val localApp = appDao.getAppById(app.id)
                if (localApp == null || !localApp.isModifiedLocally) {
                    appDao.insertApp(AppEntity.fromDomainModel(app).copy(
                        lastSyncedAt = System.currentTimeMillis(),
                        isModifiedLocally = false
                    ))
                }
            }
        } catch (e: Exception) {
            // Log error or handle retry logic
        }
    }

    override suspend fun syncAppById(appId: String) {
        try {
            // Check if local app needs to be pushed to Firestore
            val localApp = appDao.getAppById(appId)
            if (localApp != null && localApp.isModifiedLocally) {
                val appDto = AppDto.fromApp(localApp.toDomainModel())
                appsCollection.document(appId).set(appDto, SetOptions.merge()).await()
                appDao.updateSyncStatus(appId, System.currentTimeMillis(), false)
            }

            // Get fresh data from Firestore
            val remoteDoc = appsCollection.document(appId).get().await()
            if (remoteDoc.exists()) {
                val remoteApp = remoteDoc.toObject(AppDto::class.java)
                if (remoteApp != null) {
                    val app = remoteApp.toApp()

                    // Don't overwrite locally modified data
                    if (localApp == null || !localApp.isModifiedLocally) {
                        appDao.insertApp(AppEntity.fromDomainModel(app).copy(
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

    override suspend fun updateAppStatus(appId: String, status: String): Result<Unit> {
        return try {
            // Update status in Firestore
            appsCollection.document(appId).update("status", status).await()

            // Update local database
            val localApp = appDao.getAppById(appId)
            if (localApp != null) {
                val updatedApp = localApp.copy(
                    status = status,
                    updatedAt = System.currentTimeMillis()
                )
                appDao.updateApp(updatedApp)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAppIcon(appId: String, iconPath: String): Result<String> {
        return try {
            // Extract file extension
            val fileExtension = iconPath.substringAfterLast('.', "png")

            // Create storage reference
            val iconRef = storage.reference
                .child("app_images")
                .child(appId)
                .child("icon.$fileExtension")

            // Upload file
            val uploadTask = iconRef.putFile(Uri.parse(iconPath)).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            // Update app with new icon URL
            appsCollection.document(appId).update("icon_url", downloadUrl).await()

            // Update local database
            val localApp = appDao.getAppById(appId)
            if (localApp != null) {
                val updatedApp = localApp.copy(
                    iconUrl = downloadUrl,
                    updatedAt = System.currentTimeMillis()
                )
                appDao.updateApp(updatedApp)
            }

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAppScreenshot(
        appId: String,
        screenshotPath: String,
        description: String
    ): Result<String> {
        return try {
            // Extract file extension
            val fileExtension = screenshotPath.substringAfterLast('.', "jpg")
            val timestamp = System.currentTimeMillis()
            val fileName = "screenshot_$timestamp.$fileExtension"

            // Create storage reference
            val screenshotRef = storage.reference
                .child("app_images")
                .child(appId)
                .child("screenshots")
                .child(fileName)

            // Upload file
            val uploadTask = screenshotRef.putFile(Uri.parse(screenshotPath)).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            // Create screenshot object
            val screenshot = mapOf(
                "url" to downloadUrl,
                "description" to description,
                "timestamp" to timestamp.toString()
            )

            // Update app with new screenshot
            appsCollection.document(appId).update(
                "screenshots",
                com.google.firebase.firestore.FieldValue.arrayUnion(screenshot)
            ).await()

            // Update local database
            val localApp = appDao.getAppById(appId)
            if (localApp != null) {

//                val updatedScreenshots: MutableList<Screenshot>  = localApp.screenshots.orEmpty().toMutableList().apply {
//                    add(
//                        Screenshot(
//                            url = downloadUrl,
//                            description = description,
//                            timestamp = timestamp
//                        )
//                    )
//                }
//
//                val updatedApp = localApp.copy(
//                    screenshots = updatedScreenshots,
//                    updatedAt = System.currentTimeMillis()
//                )
//                appDao.updateApp(updatedApp)
            }

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pickApp(appId: String): Result<Unit> {
        return try {
            // Get current user ID from FirebaseAuth
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not logged in"))

            // Create a document in the user_picked_apps collection
            firestore.collection("user_picked_apps")
                .document("${userId}_${appId}")
                .set(
                    mapOf(
                        "userId" to userId,
                        "appId" to appId,
                        "pickedAt" to com.google.firebase.Timestamp.now()
                    )
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPickedAppsFlow(): Flow<Resource<List<App>>> = flow {
        emit(Resource.Loading())

        try {
            // Get current user ID
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                ?: throw IllegalStateException("User not logged in")

            // Query the user_picked_apps collection
            val pickedAppDocs = firestore.collection("user_picked_apps")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Get the app IDs
            val appIds = pickedAppDocs.documents.mapNotNull { it.getString("appId") }

            // If no picked apps, emit empty list
            if (appIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Fetch the actual apps
            val apps = appIds.mapNotNull { appId ->
                getAppById(appId).getOrNull()
            }

            emit(Resource.Success(apps))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to get picked apps: ${e.message}"))
        }
    }

    override suspend fun incrementActiveTesters(appId: String, count: Int): Result<Unit> {
        return try {
            // Update active tester count in Firestore
            firestore.collection("apps").document(appId)
                .update("activeTesters", FieldValue.increment(count.toLong()))
                .await()

            // If you have a local cache of apps, update it here as well

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}