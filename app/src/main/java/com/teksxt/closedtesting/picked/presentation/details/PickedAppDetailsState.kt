package com.teksxt.closedtesting.picked.presentation.details

import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.picked.domain.model.PickedApp

data class PickedAppDetailsState(
    val pickedApp: PickedApp? = null,
    val app: App? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Feedback state model
data class FeedbackState(
    val feedback: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null
)