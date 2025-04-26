package com.teksxt.closedtesting.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.teksxt.closedtesting.data.local.converter.DateConverter
import com.teksxt.closedtesting.data.local.converter.DeviceListConverter
import com.teksxt.closedtesting.data.local.converter.NotificationPreferencesConverter
import com.teksxt.closedtesting.data.local.converter.StringListConverter
import com.teksxt.closedtesting.data.local.converter.TimestampConverter
import com.teksxt.closedtesting.data.local.dao.*
import com.teksxt.closedtesting.data.local.entity.*
import com.teksxt.closedtesting.explore.data.local.converter.ScreenshotListConverter
import com.teksxt.closedtesting.explore.data.local.dao.AppDao
import com.teksxt.closedtesting.explore.data.local.entity.AppEntity
import com.teksxt.closedtesting.myrequest.data.local.dao.AssignedTesterDao
import com.teksxt.closedtesting.myrequest.data.local.dao.RequestDao
import com.teksxt.closedtesting.myrequest.data.local.entity.AssignedTesterEntity
import com.teksxt.closedtesting.myrequest.data.local.entity.RequestEntity
import com.teksxt.closedtesting.picked.data.local.dao.PickedAppDao
import com.teksxt.closedtesting.picked.data.local.entity.PickedAppEntity

@Database(
    entities = [
        UserEntity::class,
        SubscriptionEntity::class,
        RequestEntity::class,
        AssignedTesterEntity::class,
        AppEntity::class,
        PickedAppEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class, StringListConverter::class, DeviceListConverter::class,
    NotificationPreferencesConverter::class, ScreenshotListConverter::class, TimestampConverter::class)
abstract class TestSyncDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun requestDao(): RequestDao
    abstract fun assignedTesterDao(): AssignedTesterDao
    abstract fun appDao(): AppDao
    abstract fun pickedAppDao(): PickedAppDao

    companion object {
        const val DATABASE_NAME = "testsync_db"
    }
}