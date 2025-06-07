package com.teksxt.closedtesting.presentation.auth

import com.google.firebase.auth.FirebaseAuth
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

@Singleton
class SessionManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    val currentUserId: String?
        get() = (auth.currentUser?.uid ?: "").toString()

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