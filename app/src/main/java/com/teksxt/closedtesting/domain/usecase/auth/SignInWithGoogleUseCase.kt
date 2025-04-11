package com.teksxt.closedtesting.domain.usecase.auth

import com.teksxt.closedtesting.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Sign in with Google using the provided ID token
     * @param idToken The ID token obtained from Google Sign-In
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(idToken: String): Result<Unit> {
        return authRepository.signInWithGoogle(idToken)
    }
    
    /**
     * Sign in with Google without an explicit ID token
     * Note: This implementation relies on the AuthRepository to handle
     * token retrieval internally.
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(): Result<Unit> {
        // For this to work, make sure the AuthRepository has a method
        // that can handle Google authentication without an explicit token
        return authRepository.signInWithGoogleSilent()
    }
}