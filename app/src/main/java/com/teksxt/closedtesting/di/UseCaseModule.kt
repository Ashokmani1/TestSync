package com.teksxt.closedtesting.di

import com.teksxt.closedtesting.domain.repository.AuthRepository
import com.teksxt.closedtesting.domain.usecase.auth.GetCurrentUserUseCase
import com.teksxt.closedtesting.domain.usecase.auth.LoginUseCase
import com.teksxt.closedtesting.domain.usecase.auth.LogoutUseCase
import com.teksxt.closedtesting.domain.usecase.auth.SignInWithGoogleUseCase
import com.teksxt.closedtesting.domain.usecase.auth.SignUpUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidateEmailUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidateNameUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidatePasswordUseCase
import com.teksxt.closedtesting.domain.usecase.validation.ValidateUrlUseCase
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

}