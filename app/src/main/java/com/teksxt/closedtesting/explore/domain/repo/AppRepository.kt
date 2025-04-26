package com.teksxt.closedtesting.explore.domain.repo


import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun createApp(app: App): Result<App>
    suspend fun updateApp(app: App): Result<App>
    suspend fun getAppById(appId: String): Result<App?>
    suspend fun getAppsByOwner(userId: String): Result<List<App>>
    suspend fun getAppsByStatus(statuses: List<String>): Result<List<App>>
    suspend fun getAllApps(): Result<List<App>>
    suspend fun deleteApp(appId: String): Result<Unit>
    fun getAppByIdFlow(appId: String): Flow<Resource<App>>
    fun getAppsByOwnerFlow(userId: String): Flow<Resource<List<App>>>
    fun getAllAppsFlow(): Flow<Resource<List<App>>>
    suspend fun syncAppData()
    suspend fun syncAppById(appId: String)
    suspend fun updateAppStatus(appId: String, status: String): Result<Unit>
    suspend fun uploadAppIcon(appId: String, iconPath: String): Result<String>
    suspend fun uploadAppScreenshot(appId: String, screenshotPath: String, description: String): Result<String>
    suspend fun pickApp(appId: String): Result<Unit>
    fun getPickedAppsFlow(): Flow<Resource<List<App>>>
    // This should be added to the AppRepository interface
    suspend fun incrementActiveTesters(appId: String, count: Int = 1): Result<Unit>
}