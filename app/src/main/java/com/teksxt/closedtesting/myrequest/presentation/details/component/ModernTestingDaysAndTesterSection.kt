package com.teksxt.closedtesting.myrequest.presentation.details.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.presentation.details.RequestDetailsViewModel

enum class TesterFilter { NOT_COMPLETED, ALL, COMPLETED, }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModernTestingDaysAndTesterSection(
    viewModel: RequestDetailsViewModel,
    request: Request,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    navController: NavController,
    onSendBulkReminder: (Int) -> Unit,
    modifier: Modifier = Modifier
) {

    val (selectedFilter, setSelectedFilter) = remember { mutableStateOf(TesterFilter.NOT_COMPLETED) }

    val isSendingReminders by viewModel.sendBulkReminderLoading.collectAsStateWithLifecycle()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- DAYS SECTION ---
            // Animated header
            AnimatedHeader(
                selectedDay = selectedDay,
                totalDays = request.testingDays
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Modern day selector pills
            ModernDaySelector(
                currentDay = request.currentDay,
                maxDays = request.testingDays,
                selectedDay = selectedDay,
                completedDays = request.completedDays,
                onDaySelected = onDaySelected
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Day summary with animation
            AnimatedContent(
                targetState = selectedDay,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                            fadeOut(animationSpec = tween(300))
                },
                label = "day-summary-transition"
            ) { day ->
                ModernDaySummary(
                    dayNumber = day,
                    totalDays = request.testingDays,
                    currentDay = request.currentDay,
                    viewModel = viewModel,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- TESTERS SECTION ---
            // Elegant divider with label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )

                Text(
                    text = "TESTERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val allTesters = viewModel.getAssignedTesters(selectedDay)

            // Send reminders button & status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day testers label with animated counting
                AnimatedContent(
                    targetState = selectedDay,
                    transitionSpec = {
                        fadeIn() with fadeOut()
                    },
                    label = "day-testers-transition"
                ) { day ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Day $day Testers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (allTesters.isNotEmpty() && request.currentDay == selectedDay && selectedFilter == TesterFilter.NOT_COMPLETED)
                {
                    // Send reminders button
                    FilledTonalButton(
                        onClick = { onSendBulkReminder(selectedDay) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        enabled = !isSendingReminders
                    ) {
                        if (isSendingReminders) {
                            // Show progress indicator when sending
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sending...",
                                style = MaterialTheme.typography.labelMedium
                            )
                        } else {
                            // Normal button state
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Send Reminders",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // In the Row with FilterChips, update each FilterChip to include the count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                // Get count for each filter type

                val completedCount = allTesters.count { it.hasCompleted }
                val pendingCount = allTesters.size - completedCount

                FilterChip(
                    selected = selectedFilter == TesterFilter.NOT_COMPLETED,
                    onClick = { setSelectedFilter(TesterFilter.NOT_COMPLETED) },
                    label = { Text("Pending ($pendingCount)") },
                    leadingIcon = if (selectedFilter == TesterFilter.NOT_COMPLETED) {
                        {
                            Icon(
                                imageVector = Icons.Default.Pending,
                                modifier = Modifier.size(18.dp),
                                contentDescription = null
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )

                FilterChip(
                    selected = selectedFilter == TesterFilter.ALL,
                    onClick = { setSelectedFilter(TesterFilter.ALL) },
                    label = { Text("All (${allTesters.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )

                FilterChip(
                    selected = selectedFilter == TesterFilter.COMPLETED,
                    onClick = { setSelectedFilter(TesterFilter.COMPLETED) },
                    label = { Text("Completed ($completedCount)") },
                    leadingIcon = if (selectedFilter == TesterFilter.COMPLETED) {
                        {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated testers list
            AnimatedContent(
                targetState = Pair(selectedDay, selectedFilter),
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                            fadeOut(animationSpec = tween(300))
                },
                label = "testers-transition"
            ) { (day, filter) ->
                // Get testers for this day
                val allDayTesters = viewModel.getAssignedTesters(day)

                // Apply filter
                val filteredTesters = when (filter) {
                    TesterFilter.ALL -> allDayTesters
                    TesterFilter.COMPLETED -> allDayTesters.filter { it.hasCompleted }
                    TesterFilter.NOT_COMPLETED -> allDayTesters.filter { !it.hasCompleted }
                }

                // Display the filtered testers
                ModernTestersList(
                    testers = filteredTesters,
                    requestId = request.id,
                    filter = filter,
                    dayNumber = day,
                    currentDay = request.currentDay,
                    navController = navController,
                    onSendReminder = { assignedTester ->
                        viewModel.sendReminder(assignedTester.id, selectedDay)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedHeader(
    selectedDay: Int,
    totalDays: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Icon with animated rotation
        var rotation by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(selectedDay) {
            // Animate rotation when day selection changes
            val targetRotation = rotation + 360f
            animate(
                initialValue = rotation,
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = FastOutSlowInEasing
                )
            ) { value, _ ->
                rotation = value
            }
        }

        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    rotationY = rotation
                }
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Animated title change
        AnimatedContent(
            targetState = selectedDay,
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            },
            label = "title-transition"
        ) { day ->
            Text(
                text = "Testing Day $day",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Progress indicator
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Text(
                text = "$selectedDay/$totalDays",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}