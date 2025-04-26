package com.teksxt.closedtesting.picked.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.explore.domain.repo.AppRepository
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.picked.data.local.dao.PickedAppDao
import com.teksxt.closedtesting.picked.data.repo.PickedAppRepositoryImpl
import com.teksxt.closedtesting.picked.domain.repo.PickedAppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PickedAppRepositoryModule {

    @Provides
    @Singleton
    fun providePickedAppRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        pickedAppDao: PickedAppDao,
        appRepository: AppRepository,
        requestRepository: RequestRepository
    ): PickedAppRepository {
        return PickedAppRepositoryImpl(firestore, auth, pickedAppDao, appRepository, requestRepository)
    }
}