package com.teksxt.closedtesting.picked.presentation.list

import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.myrequest.domain.model.TestingStatus
import com.teksxt.closedtesting.picked.domain.model.PickedApp

data class PickedAppListState(
    val pickedApps: List<PickedAppWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class PickedAppWithDetails(
    val pickedApp: PickedApp,
    val app: App? = null,
    val testingStatus: TestingStatus = TestingStatus.PENDING
) {
    val id: String get() = pickedApp.id
    val appId: String get() = pickedApp.appId
    val name: String get() = app?.name ?: appId
    val description: String get() = app?.description ?: ""
    val iconUrl: String? get() = app?.iconUrl
//    val category: String? get() = app?.category
    val status get() = pickedApp.status
    val completionRate: Float get() = pickedApp.completionRate
    val currentTestDay: Int get() = pickedApp.currentTestDay
    val isPinned: Boolean get() = pickedApp.isPinned
    val totalTesters: Int? get() = app?.totalTesters
    val activeTesters: Int? get() = app?.activeTesters

    fun getDisplayStatus(): String {
        return when (testingStatus) {
            TestingStatus.COMPLETED -> "COMPLETED"
            else -> "ACTIVE"
        }
    }
}