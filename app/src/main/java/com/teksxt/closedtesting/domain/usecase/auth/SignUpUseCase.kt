package com.teksxt.closedtesting.domain.usecase.auth

import com.teksxt.closedtesting.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<Unit> {
        return authRepository.signUp(name, email, password)
    }
}