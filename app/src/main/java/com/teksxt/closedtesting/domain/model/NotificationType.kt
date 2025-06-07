package com.teksxt.closedtesting.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NotificationType(val displayName: String, val icon: ImageVector) {
    ALL("All", Icons.Filled.Notifications),
    CHAT("Messages", Icons.Filled.Chat),
    REMINDER("Reminders", Icons.Filled.Alarm)
}