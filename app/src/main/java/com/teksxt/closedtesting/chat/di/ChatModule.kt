package com.teksxt.closedtesting.chat.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.chat.data.repository.FirebaseChatRepository
import com.teksxt.closedtesting.chat.domain.repository.ChatRepository
import com.teksxt.closedtesting.service.NotificationService
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatModule {

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        userRepository: UserRepository,
        notificationService: NotificationService
    ): ChatRepository {
        return FirebaseChatRepository(firestore, storage, auth, userRepository, notificationService)
    }
}