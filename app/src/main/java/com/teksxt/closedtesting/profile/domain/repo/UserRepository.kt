package com.teksxt.closedtesting.profile.domain.repo

import com.teksxt.closedtesting.profile.domain.model.User
import com.teksxt.closedtesting.domain.model.UserRole
import com.teksxt.closedtesting.profile.domain.model.UserType
import com.teksxt.closedtesting.profile.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun updateUserType(userId: String, userType: UserType): Result<Unit>
    suspend fun updateUserOnboardingStatus(userId: String, isOnboarded: Boolean): Result<Unit>
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun getCurrentUser(): User?
    fun observeCurrentUser(): Flow<User?>

    suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit>
    suspend fun updateUserProfile(userId: String, profile: Map<String, Any>): Result<Unit>
    suspend fun getCurrentUserRole(): Result<UserRole?>
    suspend fun completeUserProfile(userId: String, profileData: Map<String, Any>): Result<Unit>
    suspend fun checkIfUserOnboarded(userId: String): Result<Boolean>

    suspend fun updateUserProfile(name: String, photoUrl: String?): Result<Unit>
    suspend fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateUserPreferences(preferences: UserPreferences): Result<Unit>
    suspend fun logout(): Result<Unit>
}