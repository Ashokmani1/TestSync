package com.teksxt.closedtesting.myrequest.domain.helper

import com.teksxt.closedtesting.myrequest.data.local.entity.TestDetailEntity
import com.teksxt.closedtesting.myrequest.domain.model.DayTestDetail
import java.util.UUID

fun DayTestDetail.toEntity(requestId: String): TestDetailEntity
{
    return TestDetailEntity(
        id = UUID.randomUUID().toString(), // Generate a unique ID for the entity
        requestId = requestId,
        testerId = testerId,
        testerName = testerName,
        timestamp = timestamp,
        screenshotUrl = screenshotUrl,
        feedback = feedback,
        day = day
    )
}

fun TestDetailEntity.toDomainModel(): DayTestDetail
{
    return DayTestDetail(
        testerId = testerId,
        testerName = testerName,
        timestamp = timestamp,
        screenshotUrl = screenshotUrl,
        feedback = feedback,
        day = day
    )
}