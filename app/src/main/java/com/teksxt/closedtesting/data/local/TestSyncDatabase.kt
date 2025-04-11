package com.teksxt.closedtesting.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.teksxt.closedtesting.assignedtests.data.local.dao.TestDao
import com.teksxt.closedtesting.assignedtests.data.local.entity.AssignedTestEntity
import com.teksxt.closedtesting.assignedtests.data.local.entity.DayTestEntity
import com.teksxt.closedtesting.data.local.converter.DateConverter
import com.teksxt.closedtesting.data.local.converter.StringListConverter
import com.teksxt.closedtesting.data.local.dao.*
import com.teksxt.closedtesting.data.local.entity.*
import com.teksxt.closedtesting.explore.data.local.dao.AppDao
import com.teksxt.closedtesting.explore.data.local.entity.AppEntity
import com.teksxt.closedtesting.explore.data.local.entity.PickedAppEntity
import com.teksxt.closedtesting.myrequest.data.local.dao.AssignedTesterDao
import com.teksxt.closedtesting.myrequest.data.local.dao.RequestDao
import com.teksxt.closedtesting.myrequest.data.local.dao.TestDetailsDao
import com.teksxt.closedtesting.myrequest.data.local.entity.AssignedTesterEntity
import com.teksxt.closedtesting.myrequest.data.local.entity.RequestEntity
import com.teksxt.closedtesting.myrequest.data.local.entity.TestDetailEntity

@Database(
    entities = [
        UserEntity::class,
        SubscriptionEntity::class,
        RequestEntity::class,
        AssignedTesterEntity::class,
        TestDetailEntity::class,
        AppEntity::class,
        PickedAppEntity::class,
        AssignedTestEntity::class,
        DayTestEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(DateConverter::class, StringListConverter::class)
abstract class TestSyncDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun requestDao(): RequestDao
    abstract fun assignedTesterDao(): AssignedTesterDao
    abstract fun testDetailsDao(): TestDetailsDao
    abstract fun appDao(): AppDao
    abstract fun testDao(): TestDao

    companion object {
        const val DATABASE_NAME = "testsync_db"
    }
}