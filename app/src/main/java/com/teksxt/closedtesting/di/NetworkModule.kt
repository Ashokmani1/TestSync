package com.teksxt.closedtesting.di

import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.data.remote.FirestoreService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirestoreService(
        firestore: FirebaseFirestore
    ): FirestoreService {
        return FirestoreService(firestore)
    }
}