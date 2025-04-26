package com.teksxt.closedtesting.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.data.local.dao.UserDao
import com.teksxt.closedtesting.data.local.entity.UserEntity
import com.teksxt.closedtesting.data.remote.dto.UserDto
import com.teksxt.closedtesting.settings.domain.model.User
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.get

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
            // Create user in Firestore
            val userDto = UserDto.fromUser(user)
            val userData = userDtoToMap(userDto).toMutableMap().apply {
                // Add server timestamps
                put("createdAt", FieldValue.serverTimestamp())
                put("lastActive", FieldValue.serverTimestamp())
            }
            usersCollection.document(user.id).set(userData).await()

            // Save to Room database
            val userEntity = UserEntity.fromDomainModel(user)
            userDao.insertUser(userEntity)

            Result.success(user)
        } catch (e: Exception) {
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

            val updates = mutableMapOf<String, Any>("display_name" to name)

            // Handle profile photo upload if provided
            if (photoUrl != null) {
                // Extract file extension (jpg, png, etc.)
                val fileExtension = photoUrl.substringAfterLast('.', "jpg")

                // Create storage reference with unique filename
                val storageRef = storage.reference
                    .child("profile_images")
                    .child("$userId/profile.$fileExtension")

                // Upload image and get download URL
                val uploadTask = storageRef.putFile(android.net.Uri.parse(photoUrl)).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await()

                // Add to updates
                updates["photo_url"] = downloadUrl.toString()
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

            // User devices
            "devices" to userDto.devices,

            // App owner fields
            "companyName" to userDto.companyName,
            "website" to userDto.website,
            "submittedApps" to userDto.submittedApps,

            // Preferences
            "notificationPreferences" to userDto.notificationPreferences,
            "emailNotifications" to userDto.emailNotifications,
            "pushNotifications" to userDto.pushNotifications,
            "preferredLanguage" to userDto.preferredLanguage,
            "appTheme" to userDto.appTheme,

            // Subscription info
            "subscriptionTier" to userDto.subscriptionTier,
            "subscriptionStatus" to userDto.subscriptionStatus,
            "subscriptionExpiryDate" to userDto.subscriptionExpiryDate
        )
    }
}