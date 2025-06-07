package com.teksxt.closedtesting.domain.usecase.auth

import com.teksxt.closedtesting.domain.repository.AuthRepository
import javax.inject.Inject

class EmailVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun sendVerificationEmail(): Result<Unit> {
        return authRepository.sendEmailVerificationLink()
    }

    suspend fun checkEmailVerified(): Result<Boolean> {
        return authRepository.isEmailVerified()
    }

    suspend fun refreshUser(): Result<Unit> {
        return authRepository.refreshCurrentUser()
    }
}