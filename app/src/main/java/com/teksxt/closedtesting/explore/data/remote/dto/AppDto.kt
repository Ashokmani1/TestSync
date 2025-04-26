package com.teksxt.closedtesting.explore.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.explore.domain.model.Screenshot

data class AppDto(
    @DocumentId
    val appId: String = "",

    @PropertyName("name")
    val name: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("iconUrl")
    val iconUrl: String? = null,

    @PropertyName("packageName")
    val packageName: String = "",

    @PropertyName("playStoreUrl")
    val playStoreUrl: String? = null,

    @PropertyName("testApkUrl")
    val testApkUrl: String? = null,

    @PropertyName("googleGroupUrl")
    val googleGroupUrl: String? = null,

    @PropertyName("version")
    val version: String = "",

    @PropertyName("ownerUserId")
    val ownerUserId: String = "",

    @PropertyName("categoryId")
    val categoryId: String? = null,

    @PropertyName("status")
    val status: String = "DRAFT",

    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @PropertyName("minSdkVersion")
    val minSdkVersion: Int? = null,

    @PropertyName("targetSdkVersion")
    val targetSdkVersion: Int? = null,

    @PropertyName("features")
    val features: List<String>? = null,

    @PropertyName("requiredPermissions")
    val requiredPermissions: List<String>? = null,

    @PropertyName("screenshots")
    val screenshots: List<Map<String, String>>? = null,

    @PropertyName("testingInstructions")
    val testingInstructions: String? = null,

    @PropertyName("totalTesters")
    val totalTesters: Int = 0,

    @PropertyName("activeTesters")
    val activeTesters: Int = 0,

    @PropertyName("testingDays")
    val testingDays: Int = 7,

    @PropertyName("averageRating")
    val averageRating: Float? = null
) {
    companion object {
        fun fromApp(app: App): AppDto {
            return AppDto(
                appId = app.id,
                name = app.name,
                description = app.description,
                iconUrl = app.iconUrl,
                packageName = app.packageName,
                playStoreUrl = app.playStoreUrl,
                testApkUrl = app.testApkUrl,
                googleGroupUrl = app.googleGroupUrl,
                version = app.version,
                ownerUserId = app.ownerUserId,
                categoryId = app.categoryId,
                status = app.status,
                createdAt = app.createdAt?.let { Timestamp(it / 1000, 0) } ?: Timestamp.now(),
                updatedAt = app.updatedAt?.let { Timestamp(it / 1000, 0) } ?: Timestamp.now(),
                minSdkVersion = app.minSdkVersion,
                targetSdkVersion = app.targetSdkVersion,
                features = app.features,
                requiredPermissions = app.requiredPermissions,
                screenshots = app.screenshots?.map { screenshot ->
                    mapOf(
                        "url" to (screenshot.url ?: ""),
                        "description" to (screenshot.description ?: ""),
                        "timestamp" to screenshot.timestamp.toString()
                    )
                },
                testingInstructions = app.testingInstructions,
                totalTesters = app.totalTesters ?: 0,
                activeTesters = app.activeTesters ?: 0,
                testingDays = app.testingDays ?: 7,
                averageRating = app.averageRating
            )
        }
    }

    fun toApp(): App {
        return App(
            id = appId,
            name = name,
            description = description,
            iconUrl = iconUrl,
            packageName = packageName,
            playStoreUrl = playStoreUrl,
            testApkUrl = testApkUrl,
            googleGroupUrl = googleGroupUrl,
            version = version,
            ownerUserId = ownerUserId,
            categoryId = categoryId,
            status = status,
            createdAt = createdAt.seconds * 1000,
            updatedAt = updatedAt.seconds * 1000,
            minSdkVersion = minSdkVersion,
            targetSdkVersion = targetSdkVersion,
            features = features,
            requiredPermissions = requiredPermissions,
            screenshots = screenshots?.map { screenshot ->
                Screenshot(
                    url = screenshot["url"],
                    description = screenshot["description"],
                    timestamp = screenshot["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                )
            },
            testingInstructions = testingInstructions,
            totalTesters = totalTesters,
            activeTesters = activeTesters,
            testingDays = testingDays,
            averageRating = averageRating
        )
    }
}