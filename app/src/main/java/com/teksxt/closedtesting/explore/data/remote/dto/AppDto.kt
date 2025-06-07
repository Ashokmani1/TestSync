package com.teksxt.closedtesting.explore.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.teksxt.closedtesting.explore.domain.model.App

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
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED"

    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @PropertyName("testingInstructions")
    val testingInstructions: String? = null,

    @PropertyName("totalTesters")
    val totalTesters: Int = 0,

    @PropertyName("activeTesters")
    val activeTesters: Int = 0,

    @PropertyName("testingDays")
    val testingDays: Int = 7,

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
                ownerUserId = app.ownerUserId,
                categoryId = app.category,
                status = app.status,
                createdAt = app.createdAt?.let { Timestamp(it / 1000, 0) } ?: Timestamp.now(),
                updatedAt = app.updatedAt?.let { Timestamp(it / 1000, 0) } ?: Timestamp.now(),
                testingInstructions = app.testingInstructions,
                totalTesters = app.totalTesters ?: 0,
                activeTesters = app.activeTesters ?: 0,
                testingDays = app.testingDays ?: 7,
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
            ownerUserId = ownerUserId,
            category = categoryId,
            status = status,
            createdAt = createdAt.seconds * 1000,
            updatedAt = updatedAt.seconds * 1000,
            testingInstructions = testingInstructions,
            totalTesters = totalTesters,
            activeTesters = activeTesters,
            testingDays = testingDays
        )
    }
}