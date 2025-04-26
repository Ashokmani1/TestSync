package com.teksxt.closedtesting.picked.data.remote.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.teksxt.closedtesting.picked.domain.model.PickedApp

data class PickedAppDto(
    @DocumentId
    val id: String = "", // Composite ID: userId_appId

    @PropertyName("userId")
    val userId: String = "",

    @PropertyName("appId")
    val appId: String = "",

    @PropertyName("pickedAt")
    val pickedAt: Timestamp = Timestamp.now(),

    @PropertyName("lastActivityAt")
    val lastActivityAt: Timestamp? = null,

    @PropertyName("completionRate")
    val completionRate: Float = 0f,

    @PropertyName("currentTestDay")
    val currentTestDay: Int = 1,

    @PropertyName("status")
    val status: String = "ACTIVE",

    @PropertyName("pinned")
    val isPinned: Boolean = false
) {
    companion object {
        fun fromPickedApp(pickedApp: PickedApp): PickedAppDto {
            return PickedAppDto(
                id = pickedApp.id,
                userId = pickedApp.userId,
                appId = pickedApp.appId,
                pickedAt = Timestamp(pickedApp.pickedAt / 1000, 0),
                lastActivityAt = pickedApp.lastActivityAt?.let { Timestamp(it / 1000, 0) },
                completionRate = pickedApp.completionRate,
                currentTestDay = pickedApp.currentTestDay,
                status = pickedApp.status,
                isPinned = pickedApp.isPinned
            )
        }
    }

    fun toPickedApp(): PickedApp {
        return PickedApp(
            id = id,
            userId = userId,
            appId = appId,
            pickedAt = pickedAt.seconds * 1000,
            lastActivityAt = lastActivityAt?.seconds?.times(1000),
            completionRate = completionRate,
            currentTestDay = currentTestDay,
            status = status,
            isPinned = isPinned
        )
    }
}