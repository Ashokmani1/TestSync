package com.teksxt.closedtesting.myrequest.domain.model

import java.util.Date

data class Request(
    val id: String = "",
    val appId: String = "", // Added explicit appId field to reference app entity
    val appName: String = "",
    val description: String = "",
    val groupLink: String = "",
    val playStoreLink: String = "",
    val numberOfTesters: Int = 0,
    val durationInDays: Int = 0,
    val isPremium: Boolean = false,
    val createdBy: String = "",
    val createdAt: Date? = null,
    val status: String = "active" // or "completed"
)