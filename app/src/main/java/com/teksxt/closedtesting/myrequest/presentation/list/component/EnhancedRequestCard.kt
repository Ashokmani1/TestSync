package com.teksxt.closedtesting.myrequest.presentation.list.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.teksxt.closedtesting.core.presentation.component.getAppIconBackgroundColor
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.myrequest.domain.model.Request
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun EnhancedRequestCard(
    request: Request,
    app: App?,
    progress: Float,
    onClick: () -> Unit,
    onTogglePinned: (String) -> Unit = {},
    animationDelay: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    val isPinned = remember { mutableStateOf(request.isPinned ?: false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(initialAlpha = 0.3f) + expandVertically(
            expandFrom = Alignment.Top,
            initialHeight = { 0 }
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status indicator line at top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            when (request.status.lowercase()) {
                                "active" -> MaterialTheme.colorScheme.primary
                                "completed" -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // App Header with Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App icon with improved shadow
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = getAppIconBackgroundColor(app?.name ?: request.title),
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (app?.iconUrl != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(app.iconUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "App icon",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(4.dp)
                                    )
                                } else {
                                    Text(
                                        text = (app?.name ?: request.title).take(1).uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            // App name with better typography
                            Text(
                                text = app?.name ?: request.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(4.dp))

                            // Request description with better readability
                            Text(
                                text = request.description ?: "No description",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Actions column
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            // Enhanced status badge
                            EnhancedStatusBadge(status = request.status)

//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            // Pin action
//                            IconButton(
//                                onClick = {
//                                    isPinned.value = !isPinned.value
//                                    onTogglePinned(request.id)
//                                },
//                                modifier = Modifier.size(28.dp)
//                            ) {
//                                Icon(
//                                    imageVector = if (isPinned.value) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
//                                    contentDescription = if (isPinned.value) "Unpin request" else "Pin request",
//                                    tint = if (isPinned.value)
//                                        MaterialTheme.colorScheme.primary
//                                    else
//                                        MaterialTheme.colorScheme.onSurfaceVariant,
//                                    modifier = Modifier.size(20.dp)
//                                )
//                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress section with animated progress
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(1000),
                        label = "progress"
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val progressColor = when {
                                    progress != 1f -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.tertiary
                                }

                                Icon(
                                    imageVector = when {
                                        progress != 1f -> Icons.Default.Timelapse
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = progressColor,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "${(animatedProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Enhanced progress indicator
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                progress != 1f -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.tertiary
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Day info
                        InfoCard(
                            icon = Icons.Default.DateRange,
                            value = "${(animatedProgress * request.testingDays).toInt()}/${request.testingDays}",
                            label = "Days",
                            modifier = Modifier.weight(1f)
                        )

                        // Testers info
                        InfoCard(
                            icon = Icons.Default.Person,
                            value = "${request.currentTestersCount ?: 0}",
                            label = "Testers",
                            modifier = Modifier.weight(1f)
                        )

                        // Creation date info
                        request.createdAt?.let { timestamp ->
                            val date = SimpleDateFormat("MMM dd", Locale.getDefault())
                                .format(Date(timestamp))

                            InfoCard(
                                icon = Icons.Default.CalendarToday,
                                value = date,
                                label = "Created",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EnhancedStatusBadge(status: String) {
    val (backgroundColor, textColor, icon) = when (status.lowercase()) {
        "active" -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.primary,
            Icons.Default.PlayArrow
        )
        "completed" -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.CheckCircle
        )
        "pending" -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.secondary,
            Icons.Default.Schedule
        )
        else -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.error,
            Icons.Default.Error
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = status.capitalize(),
                color = textColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

fun calculateProgress(request: Request?): Float {
    if (request == null) return 0f
    if (request.status.lowercase() == "completed") return 1f

    val createdAt = request.createdAt ?: return 0f
    val currentTime = System.currentTimeMillis()
    val elapsedDays = (currentTime - createdAt) / (1000 * 60 * 60 * 24)

    return min(1f, max(0f, elapsedDays.toFloat() / request.testingDays))
}