package com.teksxt.closedtesting.picked.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.teksxt.closedtesting.picked.domain.model.PickedApp

@Entity(tableName = "picked_apps")
data class PickedAppEntity(
    @PrimaryKey
    val id: String, // Composite ID: userId_appId
    val userId: String,
    val appId: String,
    val pickedAt: Long,
    val lastActivityAt: Long? = null,
    val completionRate: Float = 0f,
    val currentTestDay: Int = 1,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, ABANDONED
    val isPinned: Boolean = false,

    // Room-specific fields for sync
    val lastSyncedAt: Long,
    val isModifiedLocally: Boolean
) {
    companion object {
        fun fromDomainModel(pickedApp: PickedApp): PickedAppEntity {
            return PickedAppEntity(
                id = pickedApp.id,
                userId = pickedApp.userId,
                appId = pickedApp.appId,
                pickedAt = pickedApp.pickedAt,
                lastActivityAt = pickedApp.lastActivityAt,
                completionRate = pickedApp.completionRate,
                currentTestDay = pickedApp.currentTestDay,
                status = pickedApp.status,
                isPinned = pickedApp.isPinned,
                lastSyncedAt = System.currentTimeMillis(),
                isModifiedLocally = false
            )
        }
    }

    fun toDomainModel(): PickedApp {
        return PickedApp(
            id = id,
            userId = userId,
            appId = appId,
            pickedAt = pickedAt,
            lastActivityAt = lastActivityAt,
            completionRate = completionRate,
            currentTestDay = currentTestDay,
            status = status,
            isPinned = isPinned
        )
    }
}