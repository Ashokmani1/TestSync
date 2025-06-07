package com.teksxt.closedtesting.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.teksxt.closedtesting.data.local.converter.DateConverter
import com.teksxt.closedtesting.data.local.converter.StringListConverter
import com.teksxt.closedtesting.data.local.converter.TimestampConverter
import com.teksxt.closedtesting.data.local.dao.*
import com.teksxt.closedtesting.data.local.entity.*
import com.teksxt.closedtesting.explore.data.local.dao.AppDao
import com.teksxt.closedtesting.explore.data.local.entity.AppEntity
import com.teksxt.closedtesting.myrequest.data.local.dao.AssignedTesterDao
import com.teksxt.closedtesting.myrequest.data.local.dao.RequestDao
import com.teksxt.closedtesting.myrequest.data.local.entity.AssignedTesterEntity
import com.teksxt.closedtesting.myrequest.data.local.entity.RequestEntity
import com.teksxt.closedtesting.notifications.data.local.dao.NotificationDao
import com.teksxt.closedtesting.notifications.data.local.entity.NotificationEntity
import com.teksxt.closedtesting.picked.data.local.dao.PickedAppDao
import com.teksxt.closedtesting.picked.data.local.entity.PickedAppEntity

@Database(
    entities = [
        UserEntity::class,
        RequestEntity::class,
        AssignedTesterEntity::class,
        AppEntity::class,
        PickedAppEntity::class,
        NotificationEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(DateConverter::class, StringListConverter::class, TimestampConverter::class)
abstract class TestSyncDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun requestDao(): RequestDao
    abstract fun assignedTesterDao(): AssignedTesterDao
    abstract fun appDao(): AppDao
    abstract fun pickedAppDao(): PickedAppDao
    abstract fun notificationDao(): NotificationDao
}