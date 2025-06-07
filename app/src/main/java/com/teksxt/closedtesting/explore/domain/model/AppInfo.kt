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
    val ownerUserId: String,
    val category: String? = null,
    val status: String = "DRAFT", // "ACTIVE", "COMPLETED"
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val testingInstructions: String? = null,
    val totalTesters: Int? = null,
    val activeTesters: Int? = null,
    val testingDays: Int? = null,
    val premiumCode: String? = null
)