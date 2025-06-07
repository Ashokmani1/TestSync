package com.teksxt.closedtesting.picked.presentation.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.teksxt.closedtesting.core.presentation.component.ActionButton
import com.teksxt.closedtesting.core.presentation.component.getAppIconBackgroundColor
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.myrequest.domain.model.TestingStatus
import com.teksxt.closedtesting.picked.domain.model.PickedApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickedAppDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChatScreen: (String?, String?, Int) -> Unit,
    viewModel: PickedAppDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val testingStatus by viewModel.testingStatus.collectAsState()
    val pickedApp = state.pickedApp
    val app = state.app
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
                    testingStatus = testingStatus,
                    onUpdateStatus = viewModel::updateStatus,
                    onNavigateToChatScreen = {
                        onNavigateToChatScreen(viewModel.requestId, viewModel.ownerUserID, pickedApp.currentTestDay)
                    }
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
//            IconButton(onClick = onDelete) {
//                Icon(
//                    imageVector = Icons.Default.Delete,
//                    contentDescription = "Unpick app",
//                    tint = MaterialTheme.colorScheme.error
//                )
//            }
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
    testingStatus: TestingStatus = TestingStatus.PENDING,
    onUpdateStatus: (String) -> Unit,
    onNavigateToChatScreen: () -> Unit,
) {
    val scrollState = rememberScrollState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {


        AppHeaderCard(pickedApp = pickedApp, app = app)

        // Progress card with status and progress controls
        ProgressCard(
            pickedApp = pickedApp,
            testingStatus = testingStatus,
            onUpdateStatus = onUpdateStatus
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Feedback,  // Use appropriate icon
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Day ${pickedApp.currentTestDay} Feedback",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feedback explanation text
                Text(
                    text = "Share your experience testing this app and provide valuable feedback to the developers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Share Feedback Button
                Button(
                    onClick = {
                        onNavigateToChatScreen()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Share Feedback",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        AppDetailsCard(
            onPlayStoreClick = {
                app?.playStoreUrl?.let {
                    if (it.isNotEmpty()) {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(it)
                        )
                        context.startActivity(intent)
                    }
                }
            },
            onGoogleGroupUrlClick = {
                app?.googleGroupUrl?.let {
                    if (it.isNotEmpty()) {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW, it.toUri()
                        )
                        context.startActivity(intent)
                    }
                }
            },
            testingDays = app?.testingDays ?: 20,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AppDetailsCard(
    onPlayStoreClick: () -> Unit,
    onGoogleGroupUrlClick: () -> Unit,
    testingDays: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionButton(
            icon = Icons.Default.Store,
            label = "Play Store",
            onClick = onPlayStoreClick
        )

        ActionButton(
            icon = Icons.Default.DateRange,
            label = "$testingDays Days"
        )

        ActionButton(
            icon = Icons.Default.Forum,
            label = "Google Group",
            onClick = onGoogleGroupUrlClick
        )
    }
}

@Composable
fun AppHeaderCard(pickedApp: PickedApp, app: App?)
{
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
                            text = "Day ${pickedApp.currentTestDay}",
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
    testingStatus: TestingStatus = TestingStatus.PENDING,
    onUpdateStatus: (String) -> Unit
) {
    val displayStatus = when (testingStatus) {
        TestingStatus.COMPLETED -> "COMPLETED"
        TestingStatus.IN_PROGRESS -> "ACTIVE"
        else -> "ACTIVE"
    }

    val statusColor = when(displayStatus) {
        "ACTIVE" -> MaterialTheme.colorScheme.primary
        "COMPLETED" -> Color(0xFF4CAF50) // Green
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
                    text = "Day ${pickedApp.currentTestDay} Status",
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
                                text = displayStatus,
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
                        listOf("ACTIVE", "COMPLETED").forEach { status ->
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
                val icon = when (displayStatus) {
                    "ACTIVE" -> Icons.Outlined.PendingActions
                    "COMPLETED" -> Icons.Outlined.CheckCircle
                    else -> Icons.Outlined.Info
                }

                val statusDescription = when (displayStatus) {
                    "ACTIVE" -> "You are currently testing this app for Day ${pickedApp.currentTestDay}. Once finished, mark the status as \"Completed\""
                    "COMPLETED" -> "You've completed testing this app"
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
                                text = displayStatus,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Spacer(modifier = Modifier.height(3.dp))
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
