package com.teksxt.closedtesting.explore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picked_apps")
data class PickedAppEntity(
    @PrimaryKey val appId: String,
    val pickedAt: Long = System.currentTimeMillis()
)