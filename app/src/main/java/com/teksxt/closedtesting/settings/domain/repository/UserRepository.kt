package com.teksxt.closedtesting.settings.domain.repository

import com.teksxt.closedtesting.settings.domain.model.User
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun updateUserOnboardingStatus(userId: String, isOnboarded: Boolean): Result<Unit>
    suspend fun getUserById(userId: String): Result<User?>
    fun getCurrentUser(): Flow<Resource<User>>
    suspend fun updateUserProfile(name: String, photoUrl: String?): Result<Unit>
    suspend fun syncUserData()
    suspend fun updateFCMToken(userId: String, token: String): Result<Unit>
    suspend fun updateUserLastActive(): Result<Unit>
}