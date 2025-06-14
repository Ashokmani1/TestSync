package com.teksxt.closedtesting.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.teksxt.closedtesting.data.auth.GoogleSignInHelper
import com.teksxt.closedtesting.data.local.TestSyncDatabase
import com.teksxt.closedtesting.data.repository.AuthRepositoryImpl
import com.teksxt.closedtesting.data.repository.UserPreferencesRepository
import com.teksxt.closedtesting.domain.repository.AuthRepository
import com.teksxt.closedtesting.presentation.auth.SessionManager
import com.teksxt.closedtesting.service.FCMTokenManager
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideGoogleSignInHelper(
        @ApplicationContext context: Context
    ): GoogleSignInHelper {
        return GoogleSignInHelper(context)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        googleSignInHelper: GoogleSignInHelper,
        userRepository: UserRepository,
        fcmTokenManager: FCMTokenManager,
        sessionManager: SessionManager,
        userPreferencesRepository: UserPreferencesRepository,
        database: TestSyncDatabase
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, googleSignInHelper, userRepository, fcmTokenManager, sessionManager, userPreferencesRepository, database)
    }
}