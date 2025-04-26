package com.teksxt.closedtesting.di

import com.teksxt.closedtesting.domain.repository.*
import com.teksxt.closedtesting.domain.usecase.auth.*
import com.teksxt.closedtesting.domain.usecase.subscription.*
import com.teksxt.closedtesting.domain.usecase.validation.*
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // Auth Use Cases
    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(authRepository: AuthRepository): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSignUpUseCase(authRepository: AuthRepository): SignUpUseCase {
        return SignUpUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSignInWithGoogleUseCase(authRepository: AuthRepository): SignInWithGoogleUseCase {
        return SignInWithGoogleUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideLogoutUseCase(authRepository: AuthRepository): LogoutUseCase {
        return LogoutUseCase(authRepository)
    }

    // Validation Use Cases
    @Provides
    @Singleton
    fun provideValidateEmailUseCase(): ValidateEmailUseCase {
        return ValidateEmailUseCase()
    }

    @Provides
    @Singleton
    fun provideValidatePasswordUseCase(): ValidatePasswordUseCase {
        return ValidatePasswordUseCase()
    }

    @Provides
    @Singleton
    fun provideValidateNameUseCase(): ValidateNameUseCase {
        return ValidateNameUseCase()
    }

    @Provides
    @Singleton
    fun provideValidateUrlUseCase(): ValidateUrlUseCase {
        return ValidateUrlUseCase()
    }


    // Subscription Use Cases
    @Provides
    @Singleton
    fun provideGetSubscriptionStatusUseCase(subscriptionRepository: SubscriptionRepository): GetSubscriptionStatusUseCase {
        return GetSubscriptionStatusUseCase(subscriptionRepository)
    }

    @Provides
    @Singleton
    fun providePurchaseSubscriptionUseCase(subscriptionRepository: SubscriptionRepository): PurchaseSubscriptionUseCase {
        return PurchaseSubscriptionUseCase(subscriptionRepository)
    }

    @Provides
    @Singleton
    fun provideValidateSubscriptionUseCase(subscriptionRepository: SubscriptionRepository): ValidateSubscriptionUseCase {
        return ValidateSubscriptionUseCase(subscriptionRepository)
    }
}