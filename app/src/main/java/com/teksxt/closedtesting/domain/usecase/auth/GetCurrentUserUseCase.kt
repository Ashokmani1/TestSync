package com.teksxt.closedtesting.domain.usecase.auth

import com.teksxt.closedtesting.profile.domain.model.User
import com.teksxt.closedtesting.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return try {
            authRepository.getCurrentUser()
        } catch (e: Exception) {
            null
        }
    }
}