package com.teksxt.closedtesting.presentation.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.teksxt.closedtesting.data.preferences.UserPreferencesManager
import com.teksxt.closedtesting.settings.domain.model.User
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

@Singleton
class SessionManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val preferencesManager: UserPreferencesManager,
    private val userRepository: UserRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = (auth.currentUser?.uid ?: preferencesManager.userId).toString()

    private var updateLastActiveJob: Job? = null

    fun startTrackingUserActivity()
    {
        updateLastActiveJob?.cancel()
        updateLastActiveJob = scope.launch {
            while (isActive) {
                userRepository.updateUserLastActive()
                delay(5.minutes) // Update every 5 minutes
            }
        }
    }

    fun stopTrackingUserActivity()
    {
        updateLastActiveJob?.cancel()
        updateLastActiveJob = null
    }

    suspend fun getCurrentUserProfile(): User? {
        val userId = currentUserId ?: return null
        return userRepository.getUserById(userId).getOrNull()
    }

    fun getUserProfileFlow(): Flow<Resource<User>>
    {
        val userId = currentUserId ?: return flow {
            emit(Resource.Error("No user logged in"))
        }

        return userRepository.getCurrentUser()
    }

    /**
     * Check if the user is logged in and their profile is set up
     */
    suspend fun isUserOnboarded(): Boolean
    {
        val userId = currentUserId ?: return false
        val user = userRepository.getUserById(userId).getOrNull() ?: return false
        return user.isOnboarded == true
    }

    /**
     * Mark user as having completed onboarding
     */
    suspend fun completeOnboarding(): Result<Unit>
    {
        val userId = currentUserId ?: return Result.failure(Exception("No user logged in"))
        return userRepository.updateUserOnboardingStatus(userId, true)
    }

    /**
     * Refreshes the user data from remote source
     */
    suspend fun refreshUserData() {
        userRepository.syncUserData()
    }
}