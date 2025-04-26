package com.teksxt.closedtesting.myrequest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.teksxt.closedtesting.data.local.converter.StringListConverter
import com.teksxt.closedtesting.myrequest.domain.model.Request

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey
    val requestId: String,
    val appId: String,
    val ownerUserId: String,
    val title: String,
    val description: String?,
    val status: String, // "DRAFT", "PENDING", "APPROVED", "IN_PROGRESS", "COMPLETED", "REJECTED"
    val requestType: String, // "FREE", "PREMIUM"
    val createdAt: Long,
    val updatedAt: Long,
    val startDate: Long?,
    val endDate: Long?,
    val testingDays: Int,
    val requiredTestersCount: Int,
    val currentTestersCount: Int,
    @TypeConverters(StringListConverter::class)
    val testerIds: List<String>?,
    val isPublic: Boolean,
    val completionRate: Float,
    val isPinned: Boolean,
    // Room-specific fields for sync
    val lastSyncedAt: Long,
    val isModifiedLocally: Boolean
) {
    companion object {
        fun fromDomainModel(request: Request): RequestEntity {
            return RequestEntity(
                requestId = request.id,
                appId = request.appId,
                ownerUserId = request.ownerUserId,
                title = request.title,
                description = request.description,
                status = request.status,
                requestType = request.requestType,
                createdAt = request.createdAt,
                updatedAt = request.updatedAt ?: System.currentTimeMillis(),
                startDate = request.startDate,
                endDate = request.endDate,
                testingDays = request.testingDays,
                requiredTestersCount = request.requiredTestersCount,
                currentTestersCount = request.currentTestersCount ?: 0,
                testerIds = request.testerIds,
                isPublic = request.isPublic ?: false,
                completionRate = request.completionRate ?: 0f,
                isPinned = request.isPinned ?: false,
                lastSyncedAt = System.currentTimeMillis(),
                isModifiedLocally = false
            )
        }
    }

    fun toDomainModel(): Request {
        return Request(
            id = requestId,
            appId = appId,
            ownerUserId = ownerUserId,
            title = title,
            description = description,
            status = status,
            requestType = requestType,
            createdAt = createdAt,
            updatedAt = updatedAt,
            startDate = startDate,
            endDate = endDate,
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