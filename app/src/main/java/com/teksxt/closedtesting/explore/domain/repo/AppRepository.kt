package com.teksxt.closedtesting.explore.domain.repo

import com.teksxt.closedtesting.explore.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getAllApps(): Flow<List<AppInfo>>
    fun getPickedApps(): Flow<List<String>>
    suspend fun pickApp(appId: String)
}