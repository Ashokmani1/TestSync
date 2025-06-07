package com.teksxt.closedtesting.picked.presentation.details


import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.picked.domain.model.PickedApp

data class PickedAppDetailsState(
    val pickedApp: PickedApp? = null,
    val app: App? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)