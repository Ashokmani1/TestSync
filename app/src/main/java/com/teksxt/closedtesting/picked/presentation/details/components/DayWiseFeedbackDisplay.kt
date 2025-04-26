package com.teksxt.closedtesting.picked.presentation.details.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.explore.domain.model.App
import com.teksxt.closedtesting.picked.domain.model.PickedApp
import com.teksxt.closedtesting.picked.presentation.details.PickedAppDetailsViewModel
import com.teksxt.closedtesting.picked.presentation.details.formatTimestamp
import com.teksxt.closedtesting.util.PermissionHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ModernFeedbackSection(
    pickedApp: PickedApp,
    app: App?,
    viewModel: PickedAppDetailsViewModel = hiltViewModel()
) {
    val feedbackState by viewModel.feedbackState.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val dayWiseFeedback by viewModel.dayWiseFeedback.collectAsState()

    val context = LocalContext.current

    // Handle feedback submission success
    LaunchedEffect(feedbackState.submitSuccess) {
        if (feedbackState.submitSuccess) {
            Toast.makeText(context, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
            viewModel.clearSubmitSuccess()
        }
    }

    // Handle errors
    LaunchedEffect(feedbackState.error) {
        feedbackState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Feedback Header with animation
        AnimatedHeader(
            currentDay = pickedApp.currentTestDay,
            selectedDay = selectedDay
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Day selector pills
        ModernDaySelector(
            currentDay = pickedApp.currentTestDay,
            maxDays = app?.testingDays ?: pickedApp.currentTestDay.coerceAtLeast(1),
            selectedDay = selectedDay,
            hasFeedback = dayWiseFeedback.keys,
            onDaySelected = viewModel::setSelectedDay
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input section
        ModernFeedbackInput(
            isSubmitting = feedbackState.isSubmitting,
            selectedDay = selectedDay ?: pickedApp.currentTestDay,
            onSubmitFeedback = { text, screenshots, day ->
                viewModel.submitFeedback(text, screenshots, day)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feedback list
        AnimatedContent(
            targetState = feedbackState.isLoading,
            transitionSpec = {
                fadeIn() with fadeOut()
            },
            label = "loading-transition"
        ) { isLoading ->
            when {
                isLoading -> LoadingFeedbackState()
                feedbackState.feedback.isEmpty() -> EmptyFeedbackState()
                else -> FeedbackList(
                    feedbacks = feedbackState.feedback,
                    selectedDay = selectedDay
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedHeader(
    currentDay: Int,
    selectedDay: Int?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        // Icon with animated rotation
        var rotation by remember { mutableStateOf(0f) }

        LaunchedEffect(selectedDay) {
            // Animate rotation when day selection changes
            if (selectedDay != null) {
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
        }

        Icon(
            imageVector = Icons.Default.Feedback,
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
                text = if (day != null) "Day $day Feedback" else "All Feedback",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Current day badge
        if (selectedDay == null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(
                    text = "Day $currentDay",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernDaySelector(
    currentDay: Int,
    maxDays: Int,
    selectedDay: Int?,
    hasFeedback: Set<Int?>,
    onDaySelected: (Int?) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All Days" option
        DayChip(
            day = null,
            isSelected = selectedDay == null,
            hasFeedback = hasFeedback.isNotEmpty(),
            enabled = true,
            onClick = { onDaySelected(null) }
        )

        // Individual days
        for (day in 1..maxDays) {
            val isEnabled = day <= currentDay
            DayChip(
                day = day,
                isSelected = selectedDay == day,
                isCurrentDay = day == currentDay,
                hasFeedback = hasFeedback.contains(day),
                enabled = isEnabled,
                onClick = { onDaySelected(day) }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DayChip(
    day: Int?,
    isSelected: Boolean,
    isCurrentDay: Boolean = false,
    hasFeedback: Boolean,
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
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isCurrentDay -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.primary
        isCurrentDay -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                text = if (day == null) "All Days" else "Day $day",
                fontWeight = if ((isSelected || isCurrentDay) && enabled) FontWeight.Bold else FontWeight.Normal
            )

            if (!enabled && day != null) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Future Day",
                    tint = contentColor,
                    modifier = Modifier.size(10.dp)
                )
            }

            // Animated dot indicator for days with feedback
            AnimatedVisibility(
                visible = hasFeedback && enabled,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
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

@Composable
private fun ModernFeedbackInput(
    isSubmitting: Boolean,
    selectedDay: Int,
    onSubmitFeedback: (String, List<Uri>, Int) -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    val selectedScreenshots = remember { mutableStateListOf<Uri>() }
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionMessage by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { selectedScreenshots.add(uri) }
        }
    )

    // Permission launchers
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Launch gallery if permissions granted
            galleryLauncher.launch("image/*")
        } else {
            permissionMessage = "Storage permission is required to select images"
            showPermissionDialog = true
        }
    }

    fun requestGalleryAccess() {
        if (PermissionHandler.hasStoragePermission(context)) {
            galleryLauncher.launch("image/*")
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                storagePermissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                storagePermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                storagePermissionLauncher.launch(arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))
            }
        }
    }


    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = { Text(permissionMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    context.startActivity(intent)
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Input field with floating hint
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("Share your feedback for Day $selectedDay...") },
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    trailingIcon = {
                        if (feedbackText.isNotEmpty()) {
                            IconButton(onClick = { feedbackText = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Selected screenshots preview
            AnimatedVisibility(
                visible = selectedScreenshots.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            "Screenshots (${selectedScreenshots.size}/5)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = { selectedScreenshots.clear() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Clear All",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedScreenshots) { uri ->
                            ScreenshotPreviewItem(
                                uri = uri,
                                onRemove = { selectedScreenshots.remove(uri) }
                            )
                        }

                        // Add more button
                        if (selectedScreenshots.size < 5) {
                            item {
                                AddScreenshotButton(
                                    onClick = { requestGalleryAccess() }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Add screenshots button
                OutlinedIconButton(
                    onClick = { requestGalleryAccess() },
                    enabled = selectedScreenshots.size < 5,
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add Screenshot"
                    )
                }

                // Submit button - takes most space
                Button(
                    onClick = {
                        if (feedbackText.isNotEmpty()) {
                            onSubmitFeedback(feedbackText, selectedScreenshots.toList(), selectedDay)
                            feedbackText = ""
                            selectedScreenshots.clear()
                        }
                    },
                    enabled = feedbackText.isNotEmpty() && !isSubmitting,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isSubmitting) "Submitting..." else "Submit Feedback",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenshotPreviewItem(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Remove button overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun AddScreenshotButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Add Screenshot",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun LoadingFeedbackState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun EmptyFeedbackState() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No feedback yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Be the first to share your thoughts",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeedbackList(
    feedbacks: List<ChatMessage>,
    selectedDay: Int?
) {
    val filteredFeedbacks = if (selectedDay != null) {
        feedbacks.filter { it.dayNumber == selectedDay }
    } else {
        feedbacks
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with count
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = "Feedback History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "${filteredFeedbacks.size}",
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        if (filteredFeedbacks.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No feedback for ${if (selectedDay != null) "Day $selectedDay" else "any days"} yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // List of feedback items
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (feedback in filteredFeedbacks) {
                    ModernFeedbackItem(feedback)
                }
            }
        }
    }
}

@Composable
private fun ModernFeedbackItem(feedback: ChatMessage) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val hasScreenshots = !feedback.imageUrl.isNullOrEmpty()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = hasScreenshots) { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row with day badge and timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Day badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = "Day ${feedback.dayNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Timestamp
                Text(
                    text = formatTimestamp(feedback.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Screenshot indicator
                if (hasScreenshots) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

//                        Text(
//                            text = "${feedback.screenshotUrls?.size}",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.primary
//                        )

                        // Show expand/collapse indicator
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Show less" else "Show more",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Feedback text
            Text(
                text = feedback.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Screenshot gallery (if expanded)
            AnimatedVisibility(
                visible = expanded && hasScreenshots,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        feedback.imageUrl?.let { urls ->
                            item {
                                FeedbackScreenshotItem(urls)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackScreenshotItem(url: String) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(150.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(8.dp))
            .shadow(
                elevation = if (isPressed) 1.dp else 4.dp,
                shape = RoundedCornerShape(8.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
            .graphicsLayer {
                // Apply subtle scale when pressed
                scaleX = if (isPressed) 0.95f else 1f
                scaleY = if (isPressed) 0.95f else 1f
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = "Screenshot",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for better text readability on image bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Fullscreen,
                    contentDescription = "Full Screen",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "View Full",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }
        }
    }
}