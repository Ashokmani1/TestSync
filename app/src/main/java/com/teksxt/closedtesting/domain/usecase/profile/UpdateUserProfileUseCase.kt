package com.teksxt.closedtesting.domain.usecase.profile

import com.teksxt.closedtesting.domain.model.UserProfile
import com.teksxt.closedtesting.profile.domain.repo.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userProfile: UserProfile): Result<Unit> {
//        return userRepository.updateUserProfile(userProfile)
        return Result.success(Unit) // Placeholder for actual implementation
    }
}