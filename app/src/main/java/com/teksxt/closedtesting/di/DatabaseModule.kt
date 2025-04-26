package com.teksxt.closedtesting.di

import android.content.Context
import androidx.room.Room
import com.teksxt.closedtesting.data.local.TestSyncDatabase
import com.teksxt.closedtesting.myrequest.data.local.dao.AssignedTesterDao
import com.teksxt.closedtesting.myrequest.data.local.dao.RequestDao
import com.teksxt.closedtesting.data.local.dao.UserDao
import com.teksxt.closedtesting.explore.data.local.dao.AppDao
import com.teksxt.closedtesting.picked.data.local.dao.PickedAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TestSyncDatabase {
        return Room.databaseBuilder(
            context,
            TestSyncDatabase::class.java,
            "testsync-database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(database: TestSyncDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideRequestDao(database: TestSyncDatabase): RequestDao
    {
        return database.requestDao()
    }

    @Provides
    fun provideAssignedTesterDao(database: TestSyncDatabase): AssignedTesterDao
    {
        return database.assignedTesterDao()
    }

    @Provides
    fun provideAppDao(database: TestSyncDatabase): AppDao
    {
        return database.appDao()
    }

    @Provides
    @Singleton
    fun providePickedAppDao(db: TestSyncDatabase): PickedAppDao
    {
        return db.pickedAppDao()
    }
}