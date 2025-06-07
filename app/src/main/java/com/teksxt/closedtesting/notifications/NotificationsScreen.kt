package com.teksxt.closedtesting.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.teksxt.closedtesting.core.presentation.component.EmptyStateMessage
import com.teksxt.closedtesting.domain.model.Notification
import com.teksxt.closedtesting.domain.model.NotificationType
import com.teksxt.closedtesting.presentation.navigation.Screen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Group notifications by date
    val groupedNotifications = state.notifications.groupBy { notification ->
        getDateGroup(notification.createdAt)
    }

    // Filter state
    var selectedFilter by remember { mutableStateOf(NotificationType.ALL) }

    // Material 3 pull-to-refresh state
    val pullRefreshState = rememberPullToRefreshState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle errors
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (state.notifications.isNotEmpty()) {
                                viewModel.markAllAsRead()
                            }
                        },
                        enabled = state.notifications.any { !it.isRead }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark all as read",
                            tint = if (state.notifications.any { !it.isRead })
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (state.notifications.isNotEmpty()) {
                                coroutineScope.launch {
                                    val result = viewModel.clearAllNotifications()
                                    if (result) {
                                        snackbarHostState.showSnackbar("All notifications cleared")
                                    }
                                }
                            }
                        },
                        enabled = state.notifications.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear all",
                            tint = if (state.notifications.isNotEmpty())
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                },
                windowInsets = WindowInsets(0,0,0,0)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Material 3 pull-to-refresh implementation
            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { viewModel.refreshNotifications() },
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Filter chips
                    FilterChips(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { filter ->
                            selectedFilter = filter
                            viewModel.filterNotifications(filter)
                        },
                        unreadCounts = state.unreadCounts
                    )

                    // Main notification list
                    if (state.filteredNotifications.isEmpty() && !state.isLoading) {
                        EmptyNotificationsState(
                            notificationType = selectedFilter,
                            onRefresh = { viewModel.refreshNotifications() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            val filteredGroups = groupedNotifications.filterKeys { dateGroup ->
                                // If we're showing all, include everything
                                if (selectedFilter == NotificationType.ALL) {
                                    true
                                } else {
                                    // Otherwise only include date groups that have notifications of the selected type
                                    groupedNotifications[dateGroup]?.any {
                                        it.type == selectedFilter.name.lowercase()
                                    } ?: false
                                }
                            }

                            filteredGroups.forEach { (dateGroup, notifications) ->
                                // Date header
                                item {
                                    DateHeader(dateGroup)
                                }

                                // Notifications for this date
                                val filteredNotifications = if (selectedFilter == NotificationType.ALL) {
                                    notifications
                                } else {
                                    notifications.filter {
                                        it.type == selectedFilter.name.lowercase()
                                    }
                                }

                                items(
                                    items = filteredNotifications,
                                    key = { it.id }
                                ) { notification ->
                                    NotificationItem(
                                        notification = notification,
                                        onNotificationClick = { notif ->
                                            viewModel.markAsRead(notif.id)
                                            handleNotificationNavigation(notif, navController)
                                        },
                                        onDismiss = { notif ->
                                            viewModel.deleteNotification(notif.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Rest of your existing composables (FilterChips, NotificationItem, etc.) remain the same

@Composable
fun FilterChips(
    selectedFilter: NotificationType,
    onFilterSelected: (NotificationType) -> Unit,
    unreadCounts: Map<NotificationType, Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationType.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(filter.displayName)

                        // Show unread count badge if there are unread notifications
                        val unreadCount = unreadCounts[filter] ?: 0
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedFilter == filter)
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else
                                            MaterialTheme.colorScheme.primary
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = if (selectedFilter == filter)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationItem(
    notification: Notification,
    onNotificationClick: (Notification) -> Unit,
    onDismiss: (Notification) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.4f }, // Reduce swipe threshold for easier dismissal
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDismiss(notification)
                true
            } else {
                false
            }
        }
    )

    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.fillMaxWidth(),
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.error.copy(
                                alpha = 0.95f
                            ), shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            },
            content = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!notification.isRead)
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
                ) {
                    Box {
                        // Unread indicator strip
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clickable { onNotificationClick(notification) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Notification icon with colored background
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(getNotificationTypeColor(notification.type))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getNotificationTypeIcon(notification.type),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = notification.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = notification.body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatTime(notification.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )

                                    if (notification.type == "chat_message") {
                                        TextButton(
                                            onClick = { onNotificationClick(notification) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                        ) {
                                            Text(
                                                text = "Reply",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun DateHeader(dateGroup: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = dateGroup,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyNotificationsState(
    notificationType: NotificationType,
    onRefresh: () -> Unit
) {

    EmptyStateMessage(
        title = "No ${notificationType.displayName} Notifications",
        message = when (notificationType) {
            NotificationType.ALL -> "You don't have any notifications yet. They'll appear here when you receive them."
            NotificationType.CHAT -> "No new messages. Check back later for chat updates."
            NotificationType.REMINDER -> "No reminders at the moment. We'll notify you of upcoming test tasks here."
        },
        icon = {

            Icon(
                imageVector = when (notificationType) {
                    NotificationType.ALL -> Icons.Outlined.Notifications
                    NotificationType.CHAT -> Icons.Outlined.ChatBubbleOutline
                    NotificationType.REMINDER -> Icons.Outlined.Notifications
                },
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
        },
        actionLabel = "Refresh",
        onAction = onRefresh
    )
}

private fun getDateGroup(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    val today = Calendar.getInstance()

    calendar.timeInMillis = timestamp

    return when {
        // Today
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"

        // Yesterday
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 -> "Yesterday"

        // This week (within the last 7 days)
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) > today.get(Calendar.DAY_OF_YEAR) - 7 -> {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
        }

        // This month
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) -> {
            SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date(timestamp))
        }

        // This year
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
            SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date(timestamp))
        }

        // Previous years
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}

private fun getNotificationTypeIcon(type: String): ImageVector {
    return when (type) {
        "chat_message" -> Icons.Outlined.ChatBubbleOutline
        "reminder" -> Icons.Outlined.Notifications
        "test_status" -> Icons.Outlined.Update
        else -> Icons.Outlined.Info
    }
}

private fun getNotificationTypeColor(type: String): Color {
    return when (type) {
        "chat_message" -> Color(0xFF4CAF50) // Green
        "reminder" -> Color(0xFFFFA000) // Amber
        "test_status" -> Color(0xFF2196F3) // Blue
        else -> Color(0xFF9E9E9E) // Gray
    }
}

private fun handleNotificationNavigation(notification: Notification, navController: NavController) {
    when (notification.type) {
        "chat_message" -> {
            // Navigate to chat screen with requestId and testerId
            navController.navigate(
                Screen.RequestDetails.route + "?requestId=${notification.requestId}&tab=chat&testerId=${notification.testerId}"
            )
        }
        "reminder" -> {
            // Navigate to request details with specific day
            navController.navigate(
                Screen.RequestDetails.route + "?requestId=${notification.requestId}&dayNumber=${notification.dayNumber}"
            )
        }
        "test_status" -> {
            // Navigate to request details
            navController.navigate(
                Screen.RequestDetails.route + "?requestId=${notification.requestId}"
            )
        }
        else -> {
            // Default navigation
            navController.navigate(Screen.Dashboard.route)
        }
    }
}