package com.teksxt.closedtesting.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.data.local.dao.UserDao
import com.teksxt.closedtesting.data.repository.UserPreferencesRepository
import com.teksxt.closedtesting.data.repository.UserRepositoryImpl
import com.teksxt.closedtesting.explore.data.local.dao.AppDao
import com.teksxt.closedtesting.explore.data.repo.AppRepositoryImpl
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.myrequest.data.local.dao.AssignedTesterDao
import com.teksxt.closedtesting.myrequest.data.local.dao.RequestDao
import com.teksxt.closedtesting.myrequest.data.repo.RequestRepositoryImpl
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.notifications.data.NotificationRepositoryImpl
import com.teksxt.closedtesting.notifications.data.local.dao.NotificationDao
import com.teksxt.closedtesting.notifications.domain.repository.NotificationRepository
import com.teksxt.closedtesting.presentation.auth.SessionManager
import com.teksxt.closedtesting.service.NotificationService
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
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
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
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
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        userDao: UserDao
    ): UserRepository
    {
        return UserRepositoryImpl(firestore, storage, auth, userDao)
    }


    @Provides
    @Singleton
    fun provideRequestRepository(
        requestDao: RequestDao,
        assignedTesterDao: AssignedTesterDao,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
    ): RequestRepository
    {
        return RequestRepositoryImpl(firestore, auth, requestDao, assignedTesterDao)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        appDao: AppDao,
    ): AppRepository
    {
        return AppRepositoryImpl(firestore, storage, appDao)
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        auth: FirebaseAuth,
        userRepository: UserRepository
    ): SessionManager {
        return SessionManager(auth, userRepository)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        notificationDao: NotificationDao
    ): NotificationRepository {
        return NotificationRepositoryImpl(firestore, auth, notificationDao)
    }
}