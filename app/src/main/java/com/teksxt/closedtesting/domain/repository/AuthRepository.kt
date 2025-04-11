package com.teksxt.closedtesting.domain.repository

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
}