package com.teksxt.closedtesting.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.teksxt.closedtesting.data.preferences.UserPreferences
import com.teksxt.closedtesting.data.remote.FirestoreService
import com.teksxt.closedtesting.data.remote.model.toDto
import com.teksxt.closedtesting.data.remote.model.toDomainModel
import com.teksxt.closedtesting.profile.domain.model.User
import com.teksxt.closedtesting.domain.model.UserRole
import com.teksxt.closedtesting.profile.domain.model.UserType
import com.teksxt.closedtesting.profile.domain.repo.UserRepository
import com.teksxt.closedtesting.service.AuthService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestoreService: FirestoreService,
    private val authService: AuthService,
    private val firebaseAuth: FirebaseAuth,
    private val pref: UserPreferences
) : UserRepository
{

    override suspend fun createUser(user: User): Result<User> = try {
        val userDto = user.toDto()
        firestoreService.createUser(user.id, userDto)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUser(user: User): Result<User> = try {
        val userDto = user.toDto()
        firestoreService.updateUser(user.id, userDto.toMap())
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserType(userId: String, userType: UserType): Result<Unit> = try {
        firestoreService.updateUser(userId, mapOf("user_type" to userType.name))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserOnboardingStatus(userId: String, isOnboarded: Boolean): Result<Unit> = try {
        firestoreService.updateUser(userId, mapOf("is_onboarded" to isOnboarded))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserById(userId: String): Result<User?> = try {
        val userDto = firestoreService.getUserById(userId)
        Result.success(userDto?.toDomainModel())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCurrentUser(): User?
    {
        println(firestoreService.getUserById(pref.getUserId() ?: "")?.toDomainModel()?.photoUrl)
        return firestoreService.getUserById(pref.getUserId() ?: "")?.toDomainModel()
    }

    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.let {
                User(
                    id = it.uid,
                    name = it.displayName ?: "",
                    email = it.email ?: "",
                    photoUrl = it.photoUrl?.toString()
                )
            })
        }
        
        firebaseAuth.addAuthStateListener(authStateListener)
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit> = try {
        firestoreService.updateUser(userId, mapOf("role" to role.name))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserProfile(userId: String, profile: Map<String, Any>): Result<Unit> = try {
        firestoreService.updateUser(userId, profile)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCurrentUserRole(): Result<UserRole?> = try {
        val currentUser = getCurrentUser() ?: return Result.success(null)
        val userDto = firestoreService.getUserById(currentUser.id)
        val roleStr = userDto?.data?.get("role") as? String
        val role = roleStr?.let { 
            try {
                UserRole.valueOf(it)
            } catch (e: Exception) {
                null
            }
        }
        Result.success(role)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // TODO delete userID.
    override suspend fun completeUserProfile(userId: String, profileData: Map<String, Any>): Result<Unit> = try {
        val updates = profileData + mapOf("is_onboarded" to true)
        firestoreService.updateUser(pref.getUserId() ?: "", updates)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun checkIfUserOnboarded(userId: String): Result<Boolean> = try {
        val userDto = firestoreService.getUserById(userId)
        val isOnboarded = userDto?.data?.get("is_onboarded") as? Boolean ?: false
        Result.success(isOnboarded)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUserProfile(name: String, photoUrl: String?): Result<Unit>
    {
        return try {
            val userId = getCurrentUser()?.id ?: return Result.failure(IllegalStateException("User not logged in"))

            val updates = mutableMapOf<String, Any>()
            name.takeIf { it.isNotBlank() }?.let { updates["name"] = it }
            
            // Handle photo upload if photoUrl is not null
            if (photoUrl != null) {
                // Upload the image to Firebase Storage
                val downloadUrl = firestoreService.uploadImageToStorage(userId, photoUrl)
                
                // Add download URL to updates
                updates["photoUrl"] = downloadUrl
            }

            firestoreService.updateUser(userId, updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserPreferences(): Flow<com.teksxt.closedtesting.profile.domain.model.UserPreferences> = callbackFlow {
        val userId = getCurrentUser()?.id ?: ""
        if (userId.isEmpty()) {
            close()
            return@callbackFlow
        }

        val listener = firestoreService.listenToUserPreferences(userId) { prefsSnapshot ->
            val enableNotifications = prefsSnapshot?.get("enableNotifications") as? Boolean ?: true
            val enableEmailUpdates = prefsSnapshot?.get("enableEmailUpdates") as? Boolean ?: true

            val prefs = com.teksxt.closedtesting.profile.domain.model.UserPreferences(
                enableNotifications = enableNotifications,
                enableEmailUpdates = enableEmailUpdates
            )

            trySend(prefs)
        }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun updateUserPreferences(preferences: com.teksxt.closedtesting.profile.domain.model.UserPreferences): Result<Unit>
    {
        return try {
            val userId = getCurrentUser()?.id ?: return Result.failure(IllegalStateException("User not logged in"))

            val updates = mapOf(
                "enableNotifications" to preferences.enableNotifications, "enableEmailUpdates" to preferences.enableEmailUpdates
            )

            firestoreService.updateUser(userId, updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = try {
        firebaseAuth.signOut()
        pref.clearAll()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}