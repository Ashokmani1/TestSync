package com.teksxt.closedtesting.myrequest.domain.model

data class DayTestDetail(
    val testerName: String,
    val timestamp: String,
    val screenshotUrl: String? = null,
    val feedback: String? = null,
    val day: Int = 1,
    val testerId: String = ""
)