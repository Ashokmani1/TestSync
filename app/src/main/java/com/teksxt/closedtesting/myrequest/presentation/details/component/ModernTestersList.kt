package com.teksxt.closedtesting.myrequest.presentation.details.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun ModernTestersList(
    testers: List<AssignedTester>,
    requestId: String,
    filter: TesterFilter,
    dayNumber: Int,
    currentDay: Int,
    navController: NavController,
    onSendReminder: (AssignedTester) -> Unit = {}
) {

    val emptyMessage = when {
        testers.isEmpty() && filter == TesterFilter.COMPLETED -> "No testers have completed this day yet"
        testers.isEmpty() && filter == TesterFilter.NOT_COMPLETED -> "All testers have completed this day!"
        else -> "No testers found"
    }

    val emptyDescription = when {
        testers.isEmpty() && filter == TesterFilter.COMPLETED ->
            "Try checking back later or send reminders to testers"
        testers.isEmpty() && filter == TesterFilter.NOT_COMPLETED ->
            "Great job! Everyone has submitted their feedback"
        else -> "Try changing your filter or assigning more testers"
    }

    if (testers.isEmpty()) {
        // Empty state
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 1.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp)
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (filter)
                        {
                            TesterFilter.COMPLETED     -> Icons.Default.CheckCircle
                            TesterFilter.NOT_COMPLETED -> Icons.Default.Pending
                            else                       -> Icons.Default.PersonOff
                        },
                        contentDescription = null,
                        tint = when (filter)
                        {
                            TesterFilter.COMPLETED     -> MaterialTheme.colorScheme.secondary
                            TesterFilter.NOT_COMPLETED -> MaterialTheme.colorScheme.primary
                            else                       -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(36.dp)
                    )
                }
    
                Spacer(modifier = Modifier.height(16.dp))
    
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
    
                Spacer(modifier = Modifier.height(8.dp))
    
                Text(
                    text = emptyDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    } else {
        // Testers list with improved design
        Column {
            testers.forEachIndexed { index, tester ->
                ModernTesterCard(
                    tester = tester,
                    requestId = requestId,
                    dayNumber = dayNumber,
                    currentDay = currentDay,
                    navController = navController,
                    onSendReminder = onSendReminder
                )

                if (index < testers.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

enum class TesterStatus {
    COMPLETED,
    MISSED,
    IN_PROGRESS
}

enum class DragAnchors { Start, Center }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModernTesterCard(
    tester: AssignedTester,
    requestId: String,
    dayNumber: Int,
    currentDay: Int,
    navController: NavController,
    onSendReminder: (AssignedTester) -> Unit = {}
) {
    // Status and color setup - keep existing code
    val status = when {
        tester.hasCompleted -> TesterStatus.COMPLETED
        dayNumber < currentDay -> TesterStatus.MISSED
        else -> TesterStatus.IN_PROGRESS
    }

    val statusColor = when(status) {
        TesterStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        TesterStatus.MISSED -> MaterialTheme.colorScheme.error
        TesterStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
    }

    val statusIcon = when(status) {
        TesterStatus.COMPLETED -> Icons.Rounded.CheckCircle
        TesterStatus.MISSED -> Icons.Rounded.ErrorOutline
        TesterStatus.IN_PROGRESS -> Icons.Rounded.Schedule
    }

    val statusText = when(status) {
        TesterStatus.COMPLETED -> "Completed"
        TesterStatus.MISSED -> "Incomplete"
        TesterStatus.IN_PROGRESS -> "In Progress"
    }

    // Animation state for reminder confirmation
    var showReminderSent by remember { mutableStateOf(false) }
    LaunchedEffect(showReminderSent) {
        if (showReminderSent) {
            delay(2000)
            showReminderSent = false
        }
    }

    // Setup for swipe actions
    val density = LocalDensity.current

    val canSwipe = status == TesterStatus.IN_PROGRESS

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()

    // Configure anchored draggable state
    val dragState = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
                DragAnchors.Start at -170.dp.toPx(density) // Increased from -80dp to -120dp
                DragAnchors.Center at 0f
            },
            positionalThreshold = { distance: Float -> distance * 0.3f }, // Makes swiping easier
            velocityThreshold = { 100.dp.toPx(density) }, // Lower threshold for easier activation
            snapAnimationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioMediumBouncy
            ),
            decayAnimationSpec = decayAnimationSpec
        )
    }

    // Handle swipe actions
    LaunchedEffect(dragState.currentValue) {
        if (dragState.currentValue == DragAnchors.Start && canSwipe) {
            onSendReminder(tester)
            showReminderSent = true
            delay(300)
            dragState.animateTo(DragAnchors.Center)
        }
    }

    // Main card structure
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Background (revealed on swipe)
        if (canSwipe) {
            Surface(
                modifier = Modifier.matchParentSize(),
                color =  MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterEnd // Align to end instead of start
                ) {
                    // Use a right-aligned approach since we're swiping from right to left
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 20.dp) // Padding from right edge
                    ) {
                        // Enhanced reminder icon with circular background
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Send reminder",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Enhanced text with better contrast
                        Column {
                            Text(
                                text = "Send Reminder",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            Text(
                                text = "to ${tester.name.split(" ").first()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Foreground card (swipeable)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (canSwipe) {
                        Modifier.anchoredDraggable(
                            state = dragState,
                            orientation = Orientation.Horizontal,
                        )
                    } else Modifier
                )
                .offset { IntOffset(dragState.offset.roundToInt(), 0) },
            colors = CardDefaults.cardColors(
                containerColor = when(status) {
                    TesterStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation =  1.dp
            ),
            shape = RoundedCornerShape(12.dp),
            border = if (status == TesterStatus.MISSED)
                BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            else null,
            onClick = {
                navController.navigate("chat/${requestId}/${tester.id}/${dayNumber}")
            }
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Top row with avatar, name, status, and chat icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar with status indicator - smaller size for better layout
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(
                                    width = 2.dp,
                                    color = statusColor.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(statusColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (tester.avatarUrl?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = tester.avatarUrl,
                                    contentDescription = "Tester avatar",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = tester.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                                .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Middle section: Name and status
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = tester.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Status indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = statusColor.copy(alpha = 0.1f),
                                contentColor = statusColor,
                                shape = RoundedCornerShape(4.dp),
                            ) {
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Last active
                            Text(
                                text = "• ${tester.lastActive ?: "Not available"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Chat icon at the end of the row
                    IconButton(
                        onClick = {
                            navController.navigate("chat/${requestId}/${tester.id}/${dayNumber}")
                        },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat with tester"
                        )
                    }
                }

                // Reminder feedback - show only when needed
                AnimatedVisibility(
                    visible = showReminderSent,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = "Reminder sent",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                // Swipe hint when not showing reminder confirmation
                if (canSwipe && !showReminderSent) {
                    Text(
                        text = "Swipe left for reminder",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

fun Dp.toPx(density: Density): Float {
    return with(density) { this@toPx.toPx() }
}