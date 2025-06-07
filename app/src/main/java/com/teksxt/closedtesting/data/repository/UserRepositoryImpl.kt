package com.teksxt.closedtesting.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.TestSyncApp
import com.teksxt.closedtesting.data.local.dao.UserDao
import com.teksxt.closedtesting.data.local.entity.UserEntity
import com.teksxt.closedtesting.data.remote.dto.UserDto
import com.teksxt.closedtesting.settings.domain.model.User
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import com.teksxt.closedtesting.util.DeviceInfoProvider
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val userDao: UserDao
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun createUser(user: User): Result<User> {
        return try {

            val currentTimestamp = System.currentTimeMillis()
            val userForRoom = user.copy(
                createdAt = currentTimestamp,
                lastActive = currentTimestamp
            )



            val userDto = UserDto.fromUser(user)
            val userData = userDtoToMap(userDto).toMutableMap().apply {
                // Add server timestamps
                put("createdAt", FieldValue.serverTimestamp())
                put("lastActive", FieldValue.serverTimestamp())
                put("lastUpdated", FieldValue.serverTimestamp())
            }

            if (user.termsAccepted)
            {
                val termsAcceptance = hashMapOf(
                    "version" to DeviceInfoProvider(TestSyncApp.instance).getAppVersion(),
                    "acceptedAt" to FieldValue.serverTimestamp(),
                    "deviceInfo" to DeviceInfoProvider(TestSyncApp.instance).getDeviceInfo()
                )

                val userData = userData.toMutableMap().apply {
                    put("termsAcceptance", termsAcceptance)
                }

                usersCollection.document(user.id).set(userData,  SetOptions.merge()).await()

                firestore.collection("termsAcceptances")
                    .document("${user.id}_${System.currentTimeMillis()}")
                    .set(
                        hashMapOf(
                            "userId" to user.id,
                            "acceptanceData" to termsAcceptance,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "isInitialAcceptance" to true // Flag to indicate this was during signup
                        )
                    )
                    .await()
            }
            else
            {
                usersCollection.document(user.id).set(userData,  SetOptions.merge()).await()
            }

            // Save to Room database
            val userEntity = UserEntity.fromDomainModel(userForRoom)
            userDao.insertUser(userEntity)

            Result.success(userForRoom)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<User> {
        return try {
            // Update user in Firestore
            val userDto = UserDto.fromUser(user)
            usersCollection.document(user.id).set(userDto, SetOptions.merge()).await()

            // Update in Room database
            val userEntity = UserEntity.fromDomainModel(user)
            userDao.updateUser(userEntity)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            // First try to get from local database
            val localUser = userDao.getUserById(userId)

            // Then fetch from Firestore and update local if needed
            val remoteDoc = usersCollection.document(userId).get().await()

            if (remoteDoc.exists()) {
                val remoteUser = remoteDoc.toObject(UserDto::class.java)

                remoteUser?.let {
                    // Convert to domain model
                    val user = it.toUser()

                    // Update local database
                    userDao.upsertUser(UserEntity.fromDomainModel(user))

                    return Result.success(user)
                }
            }

            // If Firestore fetch failed but we have local data, return that
            if (localUser != null) {
                return Result.success(localUser.toDomainModel())
            }

            Result.success(null)
        } catch (e: Exception) {
            // If we have local data and remote fetch failed, return local data
            try {
                val localUser = userDao.getUserById(userId)
                if (localUser != null) {
                    return Result.success(localUser.toDomainModel())
                }
            } catch (_: Exception) {}

            Result.failure(e)
        }
    }

    override suspend fun syncUserData() {
        try {
            val currentUserId = auth.currentUser?.uid ?: return

            // Check if we have local modifications to push to Firebase
            val localUser = userDao.getUserById(currentUserId)
            if (localUser != null && localUser.isModifiedLocally) {
                val userDto = UserDto.fromUser(localUser.toDomainModel())
                usersCollection.document(currentUserId).set(userDto, SetOptions.merge()).await()

                // Update sync status
                userDao.updateSyncStatus(currentUserId, System.currentTimeMillis(), false)
            }

            // Pull fresh data from Firebase regardless
            val remoteDoc = usersCollection.document(currentUserId).get().await()
            if (remoteDoc.exists()) {
                val remoteUser = remoteDoc.toObject(UserDto::class.java)
                remoteUser?.let {
                    val user = it.toUser()
                    val userEntity = UserEntity.fromDomainModel(user).copy(
                        lastSyncedAt = System.currentTimeMillis(),
                        isModifiedLocally = false
                    )
                    userDao.upsertUser(userEntity)
                }
            }
        } catch (e: Exception) {
            // Log error or handle retry logic
        }
    }

    override suspend fun updateUserOnboardingStatus(userId: String, isOnboarded: Boolean): Result<Unit> {
        return try {

            // Update Firestore
            usersCollection.document(userId)
                .update("onboardingCompleted", isOnboarded)
                .await()

            // Update local database
            userDao.updateUserOnboardingStatus(userId, isOnboarded)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(name: String, photoUrl: String?): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User not logged in"))

            val updates = mutableMapOf<String, Any>("displayName" to name)

            // Get the current remote user data to check for existing photo
            val remoteDoc = usersCollection.document(userId).get().await()
            val existingPhotoUrl = remoteDoc.getString("photoUrl")

            // Handle profile photo upload if provided
            if (photoUrl != null) {
                // Delete old image if it exists and is a Firebase Storage URL
                if (!existingPhotoUrl.isNullOrEmpty() && existingPhotoUrl.contains("firebasestorage")) {
                    try {
                        // Create storage reference from the URL
                        val oldImageRef = storage.getReferenceFromUrl(existingPhotoUrl)
                        oldImageRef.delete().await()
                    } catch (e: Exception) {
                        return Result.failure(e)
                    }
                }

                // Upload new image
                val imageName = "${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference
                    .child("profile_images")
                    .child("$userId/$imageName")

                // Upload image and get download URL
                storageRef.putFile(android.net.Uri.parse(photoUrl)).await()
                val downloadUrl = storageRef.downloadUrl.await()

                // Add to updates
                updates["photoUrl"] = downloadUrl.toString()
            }

            // Update Firestore
            usersCollection.document(userId).update(updates).await()

            // Update local database
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                val updatedUser = localUser.copy(
                    displayName = name,
                    photoUrl = if (photoUrl != null) {
                        // If we just uploaded a new photo, we'll get the URL in the next sync
                        // For now, keep using the local URI for UI display
                        photoUrl
                    } else {
                        localUser.photoUrl
                    },
                    isModifiedLocally = true
                )
                userDao.updateUser(updatedUser)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<Resource<User>> = flow {

        emit(Resource.Loading())

        try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                emit(Resource.Error("No authenticated user"))
                return@flow
            }

            // First emit from local database if available
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                emit(Resource.Success(localUser.toDomainModel()))
            }

            // Then try to get fresh data from Firestore
            val remoteDoc = usersCollection.document(userId).get().await()
            if (remoteDoc.exists()) {
                val remoteUser = remoteDoc.toObject(UserDto::class.java)
                if (remoteUser != null) {
                    val user = remoteUser.toUser()

                    // Update local database with fresh data
                    val userEntity = UserEntity.fromDomainModel(user).copy(
                        lastSyncedAt = System.currentTimeMillis(),
                        isModifiedLocally = false
                    )
                    userDao.upsertUser(userEntity)

                    emit(Resource.Success(user))
                }
            } else if (localUser == null) {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            // If we previously emitted Success with local data, don't emit Error
            // This preserves the offline-first approach
            e.printStackTrace()
            emit(Resource.Error("Failed to fetch user: ${e.message}"))
        }
    }

    override suspend fun updateFCMToken(userId: String, token: String): Result<Unit> {
        return try {
            // Update Firestore
            usersCollection.document(userId)
                .update(mapOf("fcmToken" to token))
                .await()

            // Update local database if needed
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                val updatedUser = localUser.copy(
                    fcmToken = token,
                    isModifiedLocally = true
                )
                userDao.updateUser(updatedUser)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updateUserLastActive(): Result<Unit>
    {
        return try
        {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User not logged in"))

            // Update directly with server timestamp
            usersCollection.document(userId)
            .set(
                mapOf("lastActive" to FieldValue.serverTimestamp()),
                SetOptions.merge()
            )
            .await()

            // Update local version with current time as placeholder
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                val updatedUser = localUser.copy(
                    lastActive = System.currentTimeMillis(),
                    isModifiedLocally = false // This was just synced to server
                )
                userDao.updateUser(updatedUser)
            }

            Result.success(Unit)
        }
        catch (e: Exception)
        {
            Result.failure(e)
        }
    }

    override suspend fun updatePushNotificationsPreference(enabled: Boolean): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User not logged in"))

            // Create a batch of updates for Firestore
            val updates = mutableMapOf<String, Any?>(
                "pushNotifications" to enabled
            )

            if (!enabled) {
                updates["fcmToken"] = ""
            } else {
                try {
                    // This is fire-and-forget, the token update will happen asynchronously
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    usersCollection.document(userId)
                                        .update("fcmToken", token)
                                        .await()

                                    // Also update local database
                                    val localUser = userDao.getUserById(userId)
                                    if (localUser != null) {
                                        val updatedUser = localUser.copy(fcmToken = token)
                                        userDao.updateUser(updatedUser)
                                    }
                                } catch (e: Exception) {
                                    Log.e("UserRepository", "Error updating FCM token: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UserRepository", "Error getting FCM token: ${e.message}")
                }
            }

            // Update notification preference immediately
            usersCollection.document(userId)
                .update(updates)
                .await()

            // Update local database
            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                val updatedUser = localUser.copy(
                    pushNotifications = enabled,
                    fcmToken = if (!enabled) "" else localUser.fcmToken,
                    isModifiedLocally = true
                )
                userDao.updateUser(updatedUser)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTermsAcceptance(userId: String, termsAcceptance: HashMap<String, Any>): Result<Unit> {
        return try {
            // Create a timestamped record of the terms acceptance with all metadata
            val termsAcceptanceData = hashMapOf(
                "termsAcceptance" to termsAcceptance,
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            // Update the user document in Firestore
            usersCollection.document(userId)
                .update(termsAcceptanceData)
                .await()


            firestore.collection("termsAcceptances")
                .document("${userId}_${System.currentTimeMillis()}")
                .set(
                    hashMapOf(
                        "userId" to userId,
                        "acceptanceData" to termsAcceptance,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            Log.d("UserRepository", "Terms acceptance updated for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating terms acceptance: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserTermsAcceptance(userId: String): Result<Map<String, Any>?> {
        return try {
            // Get user document from Firestore
            val userDoc = usersCollection.document(userId).get().await()

            if (!userDoc.exists()) {
                Log.w("UserRepository", "User document not found for terms acceptance check: $userId")
                return Result.success(null)
            }

            // Get the termsAcceptance field from the user document
            val termsAcceptance = userDoc.get("termsAcceptance") as? Map<String, Any>

            if (termsAcceptance == null) {
                // User hasn't accepted terms yet
                Log.d("UserRepository", "No terms acceptance found for user: $userId")
                return Result.success(null)
            }

            // Return the terms acceptance data
            Result.success(termsAcceptance)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting terms acceptance: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Converts UserDto to a Map for Firestore operations.
     * Note: We exclude time fields that should use serverTimestamp()
     */
    private fun userDtoToMap(userDto: UserDto): Map<String, Any?> {
        return mapOf(
            // Core user data
            "userId" to userDto.userId,
            "email" to userDto.email,
            "displayName" to userDto.displayName,
            "photoUrl" to userDto.photoUrl,
            "fcmToken" to userDto.fcmToken,
            "emailVerified" to userDto.emailVerified,
            "accountStatus" to userDto.accountStatus,
            "onboardingCompleted" to userDto.onboardingCompleted,
            "pushNotifications" to userDto.pushNotifications,
            "appTheme" to userDto.appTheme,
        )
    }
}