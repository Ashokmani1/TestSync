package com.teksxt.closedtesting.explore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val playStoreLink: String,
    val groupLink: String,
    val requiredTesters: Int,
    val currentTesters: Int,
    val testWindow: String,
    val createdBy: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

