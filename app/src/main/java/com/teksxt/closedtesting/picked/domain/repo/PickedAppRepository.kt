package com.teksxt.closedtesting.picked.domain.repo

import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.picked.domain.model.PickedApp
import com.teksxt.closedtesting.util.Resource
import kotlinx.coroutines.flow.Flow

interface PickedAppRepository {
    suspend fun pickApp(appId: String): Result<PickedApp>
    suspend fun unpickApp(id: String): Result<Unit>
    suspend fun getPickedAppById(id: String): Result<PickedApp?>
    suspend fun getUserPickedApps(): Result<List<PickedApp>>
    fun getUserPickedAppsFlow(): Flow<Resource<List<PickedApp>>>
    suspend fun getAppPickedApps(appId: String): Result<List<PickedApp>>
    suspend fun hasUserPickedApp(appId: String): Result<Boolean>
    suspend fun updatePickedAppStatus(id: String, status: String): Result<Unit>
    suspend fun updatePickedAppProgress(id: String, completionRate: Float, currentTestDay: Int): Result<Unit>
    suspend fun togglePickedAppPin(id: String): Result<Unit>
    suspend fun syncPickedAppData()
    suspend fun syncPickedAppById(id: String)
    suspend fun getPickedAppCount(appId: String): Result<Int>

    // Retrieve full App objects for picked apps
    fun getPickedAppsFlow(): Flow<Resource<List<App>>>
}