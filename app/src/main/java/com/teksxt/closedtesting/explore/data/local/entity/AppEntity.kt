package com.teksxt.closedtesting.explore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.teksxt.closedtesting.explore.domain.model.App


@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey
    val appId: String,
    val name: String,
    val description: String,
    val iconUrl: String?,
    val packageName: String,
    val playStoreUrl: String?,
    val playStoreWebUrl: String?,
    val googleGroupUrl: String?,
    val ownerUserId: String,
    val categoryId: String?,
    val status: String, // "ACTIVE", "COMPLETED"
    val createdAt: Long,
    val updatedAt: Long,
    val testingInstructions: String?,
    val totalTesters: Int,
    val activeTesters: Int,
    val testingDays: Int,

    // Room-specific fields for sync
    val lastSyncedAt: Long,
    val isModifiedLocally: Boolean
) {
    companion object {
        fun fromDomainModel(app: App): AppEntity {
            return AppEntity(
                appId = app.id,
                name = app.name,
                description = app.description,
                iconUrl = app.iconUrl,
                packageName = app.packageName,
                playStoreUrl = app.playStoreUrl,
                playStoreWebUrl = app.testApkUrl,
                googleGroupUrl = app.googleGroupUrl,
                ownerUserId = app.ownerUserId,
                categoryId = app.category,
                status = app.status,
                createdAt = app.createdAt ?: System.currentTimeMillis(),
                updatedAt = app.updatedAt ?: System.currentTimeMillis(),
                testingInstructions = app.testingInstructions,
                totalTesters = app.totalTesters ?: 0,
                activeTesters = app.activeTesters ?: 0,
                testingDays = app.testingDays ?: 7,
                lastSyncedAt = System.currentTimeMillis(),
                isModifiedLocally = false,
            )
        }
    }

    fun toDomainModel(): App {
        return App(
            id = appId,
            name = name,
            description = description,
            iconUrl = iconUrl,
            packageName = packageName,
            playStoreUrl = playStoreUrl,
            testApkUrl = playStoreWebUrl,
            googleGroupUrl = googleGroupUrl,
            ownerUserId = ownerUserId,
            category = categoryId,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            testingInstructions = testingInstructions,
            totalTesters = totalTesters,
            activeTesters = activeTesters,
            testingDays = testingDays
        )
    }
}