package com.teksxt.closedtesting.domain.repository

import com.teksxt.closedtesting.domain.model.UserModel
import com.teksxt.closedtesting.profile.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun signUp(name: String, email: String, password: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signInWithGoogleSilent(): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    fun observeAuthState(): Flow<Boolean>
    suspend fun resetPassword(email: String): Result<Unit>

    val currentUser: Flow<UserModel?>
    val isEmailVerified: Flow<Boolean>

    suspend fun signIn(email: String, password: String): Result<UserModel>
    suspend fun signUp(email: String, password: String): Result<UserModel>
    suspend fun signOut()
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun refreshUser(): Result<UserModel>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null): Result<UserModel>
}