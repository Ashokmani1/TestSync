package com.teksxt.closedtesting.explore.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.explore.data.local.dao.AppDao
import com.teksxt.closedtesting.explore.data.local.entity.AppEntity
import com.teksxt.closedtesting.explore.data.local.entity.PickedAppEntity
import com.teksxt.closedtesting.explore.domain.model.AppInfo
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val appDao: AppDao,
    private val auth: FirebaseAuth
) : AppRepository
{

    private val appsCollection = firestore.collection("apps")
    private val usersCollection = firestore.collection("users")

    override fun getAllApps(): Flow<List<AppInfo>> = flow {
        try {
            // Try to fetch from Firestore
            val snapshot = appsCollection
                .get()
                .await()

            val apps = snapshot.documents.mapNotNull { doc ->
                try {
                    AppEntity(
                        id = doc.id,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        description = doc.getString("description") ?: "",
                        playStoreLink = doc.getString("playStoreLink") ?: "",
                        groupLink = doc.getString("groupLink") ?: "",
                        requiredTesters = doc.getLong("requiredTesters")?.toInt() ?: 0,
                        currentTesters = doc.getLong("currentTesters")?.toInt() ?: 0,
                        testWindow = doc.getString("testWindow") ?: "",
                        lastUpdated = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }

            // Cache in Room
            appDao.insertApps(apps)

        } catch (e: Exception) {
            // If Firestore fetch fails, we'll use cached data
        }

        // Emit from Room (either cached or newly fetched data)
        emitAll(appDao.getAllApps().map { entities ->
            entities.map { it.toDomainModel() }
        })
    }

    override fun getPickedApps(): Flow<List<String>> {
        return appDao.getPickedApps()
    }

    override suspend fun pickApp(appId: String) {
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

                // Update Firestore
                val batch = firestore.batch()

                // Add user to app's testers
                val appRef = appsCollection.document(appId)
                batch.update(appRef, "currentTesters", FieldValue.increment(1))

                // Add app to user's picked apps
                val userRef = usersCollection.document(userId)
                batch.update(userRef, "pickedApps", FieldValue.arrayUnion(appId))

                // Execute batch
                batch.commit().await()

                // Update local cache
                appDao.insertPickedApp(PickedAppEntity(appId))

            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun AppEntity.toDomainModel() = AppInfo(
        id = id,
        name = name,
        description = description,
        playStoreLink = playStoreLink,
        groupLink = groupLink,
        requiredTesters = requiredTesters,
        currentTesters = currentTesters,
        testWindow = testWindow
    )
}