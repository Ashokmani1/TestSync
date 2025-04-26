package com.teksxt.closedtesting.myrequest.presentation.details.component

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ModernDaySelector(
    currentDay: Int,
    maxDays: Int,
    selectedDay: Int,
    completedDays: Set<Int>,
    onDaySelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val currentDayIndex = currentDay - 1

    LaunchedEffect(currentDay) {
        // Scroll to the current day position
        listState.animateScrollToItem(currentDayIndex)
        onDaySelected(currentDay)
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(maxDays) { index ->
            val day = index + 1
            val isEnabled = day <= currentDay
            val isCompleted = completedDays.contains(day)

            DayChip(
                day = day,
                isSelected = selectedDay == day,
                isCurrentDay = day == currentDay,
                isCompleted = isCompleted,
                enabled = isEnabled,
                onClick = { onDaySelected(day) }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DayChip(
    day: Int,
    isSelected: Boolean,
    isCurrentDay: Boolean = false,
    isCompleted: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    var targetScale by remember { mutableStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = springSpec,
        label = "scale"
    )

    val chipColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        isCompleted -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) // More noticeable color
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isCurrentDay -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        isCompleted -> MaterialTheme.colorScheme.tertiary // Stronger border
        isSelected -> MaterialTheme.colorScheme.primary
        isCurrentDay -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        isCompleted -> MaterialTheme.colorScheme.tertiary // Match border for completed
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isCurrentDay -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = {
            if (enabled) {
                targetScale = 1.2f
                onClick()
            }
        },
        shape = RoundedCornerShape(16.dp),
        color = chipColor,
        border = BorderStroke(width = 1.dp, color = borderColor),
        contentColor = contentColor,
        modifier = Modifier.graphicsLayer {
            scaleX = if (isSelected && enabled) scale else 1f
            scaleY = if (isSelected && enabled) scale else 1f
            alpha = if (enabled) 1f else 0.7f  // Reduce opacity for disabled chips
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Day text
            Text(
                text = "Day $day",
                fontWeight = if ((isSelected || isCurrentDay) && enabled) FontWeight.Bold else FontWeight.Normal
            )

            if (!enabled) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Future Day",
                    tint = contentColor,
                    modifier = Modifier.size(10.dp)
                )
            } else if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }

    // Reset scale after animation
    LaunchedEffect(isSelected) {
        if (isSelected && enabled) {
            // Wait for animation to complete
            kotlinx.coroutines.delay(300)
            targetScale = 1f
        }
    }
}