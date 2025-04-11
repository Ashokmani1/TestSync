package com.teksxt.closedtesting.myrequest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.teksxt.closedtesting.myrequest.domain.model.Request
import java.util.Date

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey val id: String,
    val appName: String,
    val description: String,
    val groupLink: String,
    val playStoreLink: String,
    val numberOfTesters: Int,
    val durationInDays: Int,
    val isPremium: Boolean,
    val createdBy: String,
    val createdAt: Date?,
    val status: String
) {
    fun toDomainModel(): Request
    {
        return Request(
            id = id,
            appName = appName,
            description = description,
            groupLink = groupLink,
            playStoreLink = playStoreLink,
            numberOfTesters = numberOfTesters,
            durationInDays = durationInDays,
            isPremium = isPremium,
            createdBy = createdBy,
            createdAt = createdAt,
            status = status
        )
    }

    companion object {
        fun fromDomainModel(request: Request): RequestEntity
        {
            return RequestEntity(
                id = request.id,
                appName = request.appName,
                description = request.description,
                groupLink = request.groupLink,
                playStoreLink = request.playStoreLink,
                numberOfTesters = request.numberOfTesters,
                durationInDays = request.durationInDays,
                isPremium = request.isPremium,
                createdBy = request.createdBy,
                createdAt = request.createdAt,
                status = request.status
            )
        }
    }
}