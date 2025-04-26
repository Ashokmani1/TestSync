package com.teksxt.closedtesting.explore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.teksxt.closedtesting.data.local.converter.StringListConverter
import com.teksxt.closedtesting.explore.data.local.converter.ScreenshotListConverter
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.explore.domain.model.Screenshot


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
    val version: String,
    val ownerUserId: String,
    val categoryId: String?,
    val status: String, // "DRAFT", "SUBMITTED", "APPROVED", "TESTING", "COMPLETED"
    val createdAt: Long,
    val updatedAt: Long,
    val minSdkVersion: Int?,
    val targetSdkVersion: Int?,
    @TypeConverters(StringListConverter::class)
    val features: List<String>?,
    @TypeConverters(StringListConverter::class)
    val requiredPermissions: List<String>?,
    @TypeConverters(ScreenshotListConverter::class)
    val screenshots: List<Screenshot>?,
    val testingInstructions: String?,
    val totalTesters: Int,
    val activeTesters: Int,
    val testingDays: Int,
    val averageRating: Float?,

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
                version = app.version,
                ownerUserId = app.ownerUserId,
                categoryId = app.categoryId,
                status = app.status,
                createdAt = app.createdAt ?: System.currentTimeMillis(),
                updatedAt = app.updatedAt ?: System.currentTimeMillis(),
                minSdkVersion = app.minSdkVersion,
                targetSdkVersion = app.targetSdkVersion,
                features = app.features,
                requiredPermissions = app.requiredPermissions,
                screenshots = app.screenshots,
                testingInstructions = app.testingInstructions,
                totalTesters = app.totalTesters ?: 0,
                activeTesters = app.activeTesters ?: 0,
                testingDays = app.testingDays ?: 7,
                averageRating = app.averageRating,
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
            version = version,
            ownerUserId = ownerUserId,
            categoryId = categoryId,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            minSdkVersion = minSdkVersion,
            targetSdkVersion = targetSdkVersion,
            features = features,
            requiredPermissions = requiredPermissions,
            screenshots = screenshots,
            testingInstructions = testingInstructions,
            totalTesters = totalTesters,
            activeTesters = activeTesters,
            testingDays = testingDays,
            averageRating = averageRating
        )
    }
}