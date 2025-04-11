package com.teksxt.closedtesting.domain.usecase.profile

import com.teksxt.closedtesting.domain.model.UserProfile
import com.teksxt.closedtesting.profile.domain.repo.UserRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): UserProfile {
//        return userRepository.getUserProfile(userId)
        return UserProfile()
    }
}