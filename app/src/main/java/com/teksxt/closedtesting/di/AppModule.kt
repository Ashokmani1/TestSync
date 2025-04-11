package com.teksxt.closedtesting.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.data.auth.GoogleSignInHelper
import com.teksxt.closedtesting.myrequest.data.local.dao.AssignedTesterDao
import com.teksxt.closedtesting.myrequest.data.local.dao.RequestDao
import com.teksxt.closedtesting.myrequest.data.local.dao.TestDetailsDao
import com.teksxt.closedtesting.data.preferences.UserPreferences
import com.teksxt.closedtesting.data.remote.FirestoreService
import com.teksxt.closedtesting.data.repository.AuthRepositoryImpl
import com.teksxt.closedtesting.myrequest.data.repo.RequestRepositoryImpl
import com.teksxt.closedtesting.data.repository.SubscriptionRepositoryImpl
import com.teksxt.closedtesting.data.repository.UserRepositoryImpl
import com.teksxt.closedtesting.domain.repository.AuthRepository
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.domain.repository.SubscriptionRepository
import com.teksxt.closedtesting.profile.domain.repo.UserRepository
import com.teksxt.closedtesting.explore.data.local.dao.AppDao
import com.teksxt.closedtesting.explore.data.repo.AppRepositoryImpl
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.service.AuthService
import com.teksxt.closedtesting.service.NotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideNotificationService(
        firestore: FirebaseFirestore
    ): NotificationService {
        return NotificationService(firestore)
    }
    
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }


    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirestoreService,
        storage: FirebaseAuth,
        auth: AuthService,
        userPreferences: UserPreferences
    ): UserRepository
    {
        return UserRepositoryImpl(firestore, auth, storage, userPreferences)
    }

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        firestore: FirebaseFirestore,
    ): SubscriptionRepository
    {
        return SubscriptionRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideRequestRepository(
        requestDao: RequestDao,
        assignedTesterDao: AssignedTesterDao,
        testDetailsDao: TestDetailsDao,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        notificationService: NotificationService,
        appDao: AppDao // Add AppDao parameter
    ): RequestRepository
    {
        return RequestRepositoryImpl(requestDao, assignedTesterDao, testDetailsDao, firestore, auth, notificationService, appDao)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        firestore: FirebaseFirestore,
        appDao: AppDao,
        auth: FirebaseAuth
    ): AppRepository
    {
        return AppRepositoryImpl(firestore, appDao, auth)
    }
}