package com.teksxt.closedtesting.picked.presentation.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.teksxt.closedtesting.core.presentation.component.getAppIconBackgroundColor
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.picked.domain.model.PickedApp
import com.teksxt.closedtesting.picked.presentation.details.components.ModernFeedbackSection
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickedAppDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PickedAppDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pickedApp = state.pickedApp
    val app = state.app
    val context = LocalContext.current
    var showConfirmUnpickDialog by remember { mutableStateOf(false) }

    if (showConfirmUnpickDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmUnpickDialog = false },
            title = { Text("Confirm Unpick App") },
            text = { Text("Are you sure you want to stop testing this app? Your progress will be lost.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.unpickApp { onNavigateBack() }
                        showConfirmUnpickDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Unpick App")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmUnpickDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppDetailsToolbar(
                appName = app?.name ?: pickedApp?.appId ?: "App Details",
                isPinned = pickedApp?.isPinned == true,
                onNavigateBack = onNavigateBack,
                onTogglePin = { viewModel.togglePinnedStatus() },
                onDelete = { showConfirmUnpickDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            if (state.isLoading) {
                LoadingView()
            } else if (state.error != null) {
                ErrorView(
                    error = state.error!!,
                    onRetry = {
                        pickedApp?.let { viewModel.loadPickedAppDetails(it.id) }
                    }
                )
            } else if (pickedApp != null) {
                DetailsContent(
                    pickedApp = pickedApp,
                    app = app,
                    onUpdateStatus = viewModel::updateStatus,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun AppDetailsToolbar(
    appName: String,
    isPinned: Boolean,
    onNavigateBack: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            // Title
            Text(
                text = appName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )

            // Pin button
            IconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = if (isPinned) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Toggle pin status",
                    tint = if (isPinned)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Unpick app",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
fun DetailsContent(
    pickedApp: PickedApp,
    app: App?,
    onUpdateStatus: (String) -> Unit,
    viewModel: PickedAppDetailsViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header with icon and basic info
        AppHeaderCard(pickedApp = pickedApp, app = app)

        // Progress card with status and progress controls
        ProgressCard(
            pickedApp = pickedApp,
            onUpdateStatus = onUpdateStatus
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            ModernFeedbackSection(
                pickedApp = pickedApp,
                app = app,
                viewModel = viewModel
            )
        }

        // App details
        if (app != null) {
            AppDetailsCard(app = app)
        }

        // History section
//        TimelineCard(pickedApp = pickedApp)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AppHeaderCard(pickedApp: PickedApp, app: App?) {
    val context = LocalContext.current
    var appIconUrl by remember { mutableStateOf<String?>(null) }

    // Try to load app icon
    LaunchedEffect(pickedApp.appId) {
        try {
            appIconUrl = com.teksxt.closedtesting.util.TestSyncUtil.fetchAppIconUrl(pickedApp.appId)
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (appIconUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(appIconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = app?.name ?: pickedApp.appId,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(72.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(getAppIconBackgroundColor(app?.name ?: "")),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (app?.name ?: pickedApp.appId).take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                    }
                }

                // Status indicator badge
                val statusColor = when(pickedApp.status.uppercase()) {
                    "ACTIVE" -> MaterialTheme.colorScheme.primary
                    "COMPLETED" -> Color(0xFF4CAF50) // Green
                    "ABANDONED" -> Color(0xFFF44336) // Red
                    else -> MaterialTheme.colorScheme.secondary
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (pickedApp.status.uppercase()) {
                        "ACTIVE" -> Icons.Outlined.PendingActions
                        "COMPLETED" -> Icons.Outlined.CheckCircle
                        else -> Icons.Default.Info
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = pickedApp.status,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // App details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = app?.name ?: pickedApp.appId,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (app?.description != null) {
                    Text(
                        text = app.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Testing progress pill
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Day ${pickedApp.currentTestDay} • ${(pickedApp.completionRate * 100).toInt()}% complete",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressCard(
    pickedApp: PickedApp,
    onUpdateStatus: (String) -> Unit
) {
    val statusColor = when(pickedApp.status.uppercase()) {
        "ACTIVE" -> MaterialTheme.colorScheme.primary
        "COMPLETED" -> Color(0xFF4CAF50) // Green
        "ABANDONED" -> Color(0xFFF44336) // Red
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Testing Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Status selector
                var statusExpanded by remember { mutableStateOf(false) }

                Box {
                    Surface(
                        onClick = { statusExpanded = true },
                        shape = RoundedCornerShape(16.dp),
                        color = statusColor.copy(alpha = 0.15f),
                        contentColor = statusColor,
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = pickedApp.status,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Change status",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        listOf("ACTIVE", "COMPLETED", "ABANDONED").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    onUpdateStatus(status)
                                    statusExpanded = false
                                },
                                leadingIcon = {
                                    val icon = when (status) {
                                        "ACTIVE" -> Icons.Outlined.PendingActions
                                        "COMPLETED" -> Icons.Outlined.CheckCircle
                                        "ABANDONED" -> Icons.Default.Close
                                        else -> Icons.Default.PendingActions
                                    }
                                    Icon(imageVector = icon, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status explanation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val icon = when (pickedApp.status.uppercase()) {
                    "ACTIVE" -> Icons.Outlined.PendingActions
                    "COMPLETED" -> Icons.Outlined.CheckCircle
                    "ABANDONED" -> Icons.Outlined.Cancel
                    else -> Icons.Outlined.Info
                }

                val statusDescription = when (pickedApp.status.uppercase()) {
                    "ACTIVE" -> "You're currently testing this app"
                    "COMPLETED" -> "You've completed testing this app"
                    "ABANDONED" -> "You've stopped testing this app"
                    else -> "Unknown status"
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pickedApp.status,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Text(
                                text = statusDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdateProgressControls(
    initialProgress: Float,
    initialDay: Int,
    onUpdateProgress: (Float, Int) -> Unit
) {
    var sliderPosition by remember { mutableStateOf(initialProgress) }
    var day by remember { mutableIntStateOf(initialDay) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        // Progress slider
        Text(
            text = "${(sliderPosition * 100).toInt()}% complete",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 0f..1f,
            steps = 20,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Day input with stepper
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Testing Day:",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Day stepper
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    IconButton(
                        onClick = { if (day > 1) day-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease day",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .width(56.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = day.toString(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                        )
                    }

                    IconButton(
                        onClick = { day++ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase day",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onUpdateProgress(sliderPosition, day) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save progress"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Progress")
            }
        }
    }
}

@Composable
fun AppDetailsCard(app: App)
{
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info items
//            InfoItem(
//                icon = Icons.Outlined.Category,
//                label = "Category",
//                value = app.category ?: "Unknown"
//            )

            InfoItem(
                icon = Icons.Outlined.Groups,
                label = "Testers",
                value = "${app.activeTesters}/${app.totalTesters} testers"
            )

            InfoItem(
                icon = Icons.Outlined.Schedule,
                label = "Testing Days",
                value = app.testingDays.toString()
            )

            // Action buttons (Play Store, Group link, etc.)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = {
                        app.playStoreUrl?.let {
                            if (it.isNotEmpty()) {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(it)
                                )
                                context.startActivity(intent)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = app.playStoreUrl != null && app.playStoreUrl.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Store,
                        contentDescription = "Open Play Store"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Store")
                }

                OutlinedButton(
                    onClick = {
                        app.googleGroupUrl?.let {
                            if (it.isNotEmpty()) {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse(it)
                                )
                                context.startActivity(intent)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = app.googleGroupUrl != null && app.googleGroupUrl.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = "Open Test Group"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Group")
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TimelineCard(pickedApp: PickedApp) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Testing Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline events
            TimelineEvent(
                icon = Icons.Outlined.AddCircle,
                title = "Testing Started",
                description = "You started testing this app",
                timestamp = pickedApp.pickedAt,
                isFirst = true,
                isLast = pickedApp.lastActivityAt == null || pickedApp.lastActivityAt == pickedApp.pickedAt,
                color = MaterialTheme.colorScheme.primary
            )

            if (pickedApp.lastActivityAt != null && pickedApp.lastActivityAt != pickedApp.pickedAt) {
                TimelineEvent(
                    icon = Icons.Outlined.Update,
                    title = "Last Activity",
                    description = "Day ${pickedApp.currentTestDay} progress updated to ${(pickedApp.completionRate * 100).toInt()}%",
                    timestamp = pickedApp.lastActivityAt!!,
                    isFirst = false,
                    isLast = true,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // Future timeline based on current progress
            val remainingPercentage = 1f - pickedApp.completionRate
            if (remainingPercentage > 0 && pickedApp.status.uppercase() != "COMPLETED") {
                val estimatedCompletionMillis = pickedApp.lastActivityAt ?: pickedApp.pickedAt

                TimelineEvent(
                    icon = Icons.Outlined.Event,
                    title = "Estimated Completion",
                    description = if (pickedApp.completionRate > 0) "Based on your current progress" else "Start updating your progress",
                    timestamp = null,
                    isFirst = false,
                    isLast = true,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    isDashed = true
                )
            }
        }
    }
}

@Composable
fun TimelineEvent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    timestamp: Long?,
    isFirst: Boolean,
    isLast: Boolean,
    color: Color,
    isDashed: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        val outlineVariant = MaterialTheme.colorScheme.outlineVariant

        // Timeline container with proper height
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(80.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Draw the circle first (underneath the lines)
            Surface(
                shape = CircleShape,
                color = if (isDashed) MaterialTheme.colorScheme.surface else color,
                border = if (isDashed) BorderStroke(1.dp, color) else null,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDashed) color else Color.White,
                    modifier = Modifier
                        .size(12.dp)
                        .padding(2.dp)
                )
            }

            // Line above (draw after circle to ensure it draws on top)
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            if (isDashed) Color.Transparent else color
                        )
                )

                // If dashed, draw the dash pattern on top
                if (isDashed) {
                    Canvas(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        drawLine(
                            color = outlineVariant,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
                        )
                    }
                }
            }

            // Line below
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 26.dp)
                        .background(
                            if (isDashed) Color.Transparent else color
                        )
                )

                // If dashed, draw the dash pattern on top
                if (isDashed) {
                    Canvas(
                        modifier = Modifier
                            .width(2.dp)
                            .height(60.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 26.dp)
                    ) {
                        drawLine(
                            color = outlineVariant,
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
                        )
                    }
                }
            }
        }

        // Content section
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp, start = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (timestamp != null) {
                val formattedDate = remember(timestamp) {
                    formatTimestamp(timestamp)
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


fun formatTimestamp(timestamp: Long): String {
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(timestamp),
        ZoneId.systemDefault()
    )

    val now = LocalDateTime.now()
    val daysDiff = ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())

    return when {
        daysDiff == 0L -> "Today at ${dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
        daysDiff == 1L -> "Yesterday at ${dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
        daysDiff < 7L -> "${dateTime.dayOfWeek.toString().lowercase().capitalize()} at ${dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))}"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}