package com.teksxt.closedtesting.explore.domain.model

data class AppInfo(
    val id: String,
    val name: String,
    val description: String,
    val playStoreLink: String,
    val groupLink: String,
    val requiredTesters: Int,
    val currentTesters: Int,
    val testWindow: String
)