package com.teksxt.closedtesting.myrequest.presentation.details.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RunCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teksxt.closedtesting.myrequest.presentation.details.DayStatus
import com.teksxt.closedtesting.myrequest.presentation.details.RequestDetailsViewModel


@Composable
fun ModernDaySummary(
    dayNumber: Int,
    totalDays: Int,
    currentDay: Int,
    viewModel: RequestDetailsViewModel, // Add the viewModel parameter
    modifier: Modifier = Modifier
) {
    // Get testers for this day and calculate completion status
    val testers = viewModel.getAssignedTesters(dayNumber)
    val completedCount = testers.count { it.hasCompleted }
    val totalCount = testers.size

    // Determine status based on completion
    val status = when {
        // Day is fully completed
        completedCount == totalCount && totalCount > 0 ->
            DayStatus.Completed

        // Day is in the past but not all testers completed
        dayNumber < currentDay && completedCount < totalCount && totalCount > 0 ->
            DayStatus.Expired

        // Day is in progress (current day or has some completions)
        completedCount > 0 || dayNumber == currentDay ->
            DayStatus.InProgress

        // Future day
        else ->
            DayStatus.Upcoming
    }

    // Update status colors with new Expired state
    val statusColor = when (status) {
        DayStatus.Completed -> MaterialTheme.colorScheme.tertiary
        DayStatus.InProgress -> MaterialTheme.colorScheme.primary
        DayStatus.Expired -> MaterialTheme.colorScheme.error
        DayStatus.Upcoming -> MaterialTheme.colorScheme.outline
    }

    // Update status icons
    val statusIcon = when (status) {
        DayStatus.Completed -> Icons.Rounded.CheckCircle
        DayStatus.InProgress -> Icons.Rounded.RunCircle
        DayStatus.Expired -> Icons.Outlined.ErrorOutline // Add this import
        DayStatus.Upcoming -> Icons.Outlined.WatchLater
    }

    // Update status text
    val statusText = when (status) {
        DayStatus.Completed -> "Testing completed"
        DayStatus.InProgress -> "$completedCount/$totalCount testers completed"
        DayStatus.Expired -> "Day ended ($completedCount/$totalCount completed)"
        DayStatus.Upcoming -> "Upcoming test day"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = statusColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row with day info and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = statusColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$dayNumber",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Day info
                Column {
                    Text(
                        text = "Day $dayNumber of $totalDays",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor
                        )
                    }
                }
            }

            if (status == DayStatus.Expired)
            {
                Spacer(modifier = Modifier.height(16.dp))

                // Modern error card for incomplete testers
                val incompleteTesterCount = totalCount - completedCount
                if (incompleteTesterCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Error icon
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Incomplete Testing",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "$incompleteTesterCount ${if (incompleteTesterCount == 1) "tester" else "testers"} didn't complete testing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}