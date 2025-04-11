package com.teksxt.closedtesting.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.teksxt.closedtesting.data.auth.GoogleSignInHelper
import com.teksxt.closedtesting.data.preferences.UserPreferences
import com.teksxt.closedtesting.data.remote.FirestoreService
import com.teksxt.closedtesting.data.repository.AuthRepositoryImpl
import com.teksxt.closedtesting.domain.repository.AuthRepository
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
        firestoreService: FirestoreService,
        userPreferences: UserPreferences,
        googleSignInHelper: GoogleSignInHelper
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestoreService, userPreferences, googleSignInHelper)
    }
}