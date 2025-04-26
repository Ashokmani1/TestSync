package com.teksxt.closedtesting.myrequest.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.teksxt.closedtesting.myrequest.domain.model.Request

data class RequestDto(
    @DocumentId
    val requestId: String = "",

    @PropertyName("appId")
    val appId: String = "",

    @PropertyName("ownerUserId")
    val ownerUserId: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String? = null,

    @PropertyName("status")
    val status: String = "DRAFT",

    @PropertyName("requestType")
    val requestType: String = "FREE",

    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    @PropertyName("startDate")
    val startDate: Timestamp? = null,

    @PropertyName("endDate")
    val endDate: Timestamp? = null,

    @PropertyName("testingDays")
    val testingDays: Int = 7,

    @PropertyName("requiredTestersCount")
    val requiredTestersCount: Int = 5,

    @PropertyName("currentTestersCount")
    val currentTestersCount: Int = 0,

    @PropertyName("testerIds")
    val testerIds: List<String>? = null,

    @PropertyName("isPublic")
    val isPublic: Boolean = false,

    @PropertyName("testDays")
    val testDays: List<Map<String, Any>>? = null,

    @PropertyName("completionRate")
    val completionRate: Float = 0f,

    @PropertyName("pinned")
    val isPinned: Boolean = false
) {
    companion object {
        fun fromRequest(request: Request): RequestDto {
            return RequestDto(
                requestId = request.id,
                appId = request.appId,
                ownerUserId = request.ownerUserId,
                title = request.title,
                description = request.description,
                status = request.status,
                requestType = request.requestType,
                createdAt = Timestamp(request.createdAt / 1000, 0),
                updatedAt = request.updatedAt?.let { Timestamp(it / 1000, 0) } ?: Timestamp.now(),
                startDate = request.startDate?.let { Timestamp(it / 1000, 0) },
                endDate = request.endDate?.let { Timestamp(it / 1000, 0) },
                testingDays = request.testingDays,
                requiredTestersCount = request.requiredTestersCount,
                currentTestersCount = request.currentTestersCount ?: 0,
                testerIds = request.testerIds,
                isPublic = request.isPublic ?: false,
                completionRate = request.completionRate ?: 0f,
                isPinned = request.isPinned ?: false
            )
        }
    }

    fun toRequest(): Request {
        return Request(
            id = requestId,
            appId = appId,
            ownerUserId = ownerUserId,
            title = title,
            description = description,
            status = status,
            requestType = requestType,
            createdAt = createdAt.seconds * 1000,
            updatedAt = updatedAt.seconds * 1000,
            startDate = startDate?.seconds?.times(1000),
            endDate = endDate?.seconds?.times(1000),
            testingDays = testingDays,
            requiredTestersCount = requiredTestersCount,
            currentTestersCount = currentTestersCount,
            testerIds = testerIds,
            isPublic = isPublic,
            completionRate = completionRate,
            isPinned = isPinned
        )
    }
}