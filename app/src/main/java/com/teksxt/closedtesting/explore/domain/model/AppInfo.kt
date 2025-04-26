package com.teksxt.closedtesting.explore.domain.model

data class App(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String? = null,
    val packageName: String,
    val playStoreUrl: String? = null,
    val testApkUrl: String? = null,
    val googleGroupUrl: String? = null,
    val version: String,
    val ownerUserId: String,
    val categoryId: String? = null,
    val status: String = "DRAFT", // "DRAFT", "SUBMITTED", "APPROVED", "TESTING", "COMPLETED"
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val minSdkVersion: Int? = null,
    val targetSdkVersion: Int? = null,
    val features: List<String>? = null,
    val requiredPermissions: List<String>? = null,
    val screenshots: List<Screenshot>? = null,
    val testingInstructions: String? = null,
    val totalTesters: Int? = null,
    val activeTesters: Int? = null,
    val testingDays: Int? = null,
    val averageRating: Float? = null
)

data class Screenshot(
    val url: String? = null,
    val description: String? = null,
    val timestamp: Long = 0L
)