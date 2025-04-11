package com.teksxt.closedtesting.assignedtests.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.assignedtests.data.local.dao.TestDao
import com.teksxt.closedtesting.assignedtests.data.repo.TestRepositoryImpl
import com.teksxt.closedtesting.assignedtests.domain.repo.TestRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AssignedTestModule{

    @Provides
    @Singleton
    fun provideTestRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        testDao: TestDao
    ): TestRepository
    {
        return TestRepositoryImpl(firestore, storage, auth, testDao)
    }
}