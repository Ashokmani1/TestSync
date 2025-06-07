package com.teksxt.closedtesting.explore.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.teksxt.closedtesting.R
import com.teksxt.closedtesting.core.presentation.component.EmptyContentView
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import com.teksxt.closedtesting.core.presentation.component.ShimmerLoadingScreen
import com.teksxt.closedtesting.core.presentation.component.getAppIconBackgroundColor
import com.teksxt.closedtesting.explore.domain.model.App
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val errorMessage by viewModel.errorState.collectAsState()
    val pickingAppId by viewModel.pickingAppId.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = it,
                    actionLabel = "Dismiss"
                )
                viewModel.clearError()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        Scaffold(
            topBar = {
                ExploreTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    isSearchExpanded = isSearchExpanded,
                    onSearchExpandChange = { isSearchExpanded = it },
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = viewModel::onFilterChange
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = {
                        coroutineScope.launch {
                            viewModel.refreshApps()
                        }
                    },
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    if (uiState.isLoading && uiState.apps.isEmpty()) {
                        ShimmerLoadingScreen()
                    } else if (uiState.apps.isEmpty()) {
                        EmptyState()
                    } else {
                        ModernAppsList(
                            apps = uiState.apps,
                            onPickApp = viewModel::pickApp,
                            pickedApps = uiState.pickedApps,
                            userOwnApps = uiState.userOwnApps,
                            listState = listState,
                            pickingAppId = pickingAppId
                        )
                    }
                }
            }
        }

        // Floating action button for quick pick filter
        AnimatedVisibility(
            visible = listState.canScrollBackward,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = "Scroll to top"
                )
            }
        }
    }
}

@Composable
fun ExploreTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchExpanded: Boolean,
    onSearchExpandChange: (Boolean) -> Unit,
    selectedFilter: ExploreFilter,
    onFilterSelected: (ExploreFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 16.dp)
    ) {
        // Using Box instead of Row to have better control over positioning
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Search button - always positioned at the end
            IconButton(
                onClick = { onSearchExpandChange(!isSearchExpanded) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSearchExpanded)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                    .border(
                        width = if (isSearchExpanded) 1.dp else 0.dp,
                        color = if (isSearchExpanded)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isSearchExpanded) Icons.Filled.ArrowBack else Icons.Filled.Search,
                    contentDescription = if (isSearchExpanded) "Back to explore" else "Search apps",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title - shown only when search is not expanded
            // Positioned at start with padding to not overlap the button
            if (!isSearchExpanded) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = 56.dp) // Leave space for the button
                ) {
                    Text(
                        text = "Explore Apps",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Add subtitle
                    Text(
                        text = "Discover and test new applications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            // Search field - only when expanded
            // Positioned with padding to not overlap the button
            else {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth()
                        .padding(end = 56.dp) // Leave space for the button
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 2.dp,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        val focusRequester = remember { FocusRequester() }

                        LaunchedEffect(isSearchExpanded) {
                            if (isSearchExpanded) {
                                delay(100)
                                focusRequester.requestFocus()
                            }
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search apps",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Modern filter chips with animation
        ModernFilterChips(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtle divider
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ModernFilterChips(
    selectedFilter: ExploreFilter,
    onFilterSelected: (ExploreFilter) -> Unit
) {
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExploreFilter.values().forEach { filter ->
            val selected = filter == selectedFilter
            var targetScale by remember { mutableStateOf(1f) }
            val animatedScale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = springSpec,
                label = "scale"
            )

            Surface(
                onClick = {
                    if (!selected) {
                        targetScale = 1.05f
                        onFilterSelected(filter)
                    }
                },
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = if (selected) animatedScale else 1f
                        scaleY = if (selected) animatedScale else 1f
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                tonalElevation = if (selected) 4.dp else 0.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = filter.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            LaunchedEffect(selected) {
                if (!selected) {
                    targetScale = 1f
                }
            }
        }
    }
}

@Composable
private fun ModernAppsList(
    apps: List<App>,
    onPickApp: (String) -> Unit,
    pickedApps: Set<String>,
    userOwnApps: Set<String>,
    listState: LazyListState,
    pickingAppId: String? = null
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(apps, key = { it.id }) { app ->
            val isPicked = app.id in pickedApps
            val isOwnApp = app.id in userOwnApps
            val isLoading = app.id == pickingAppId
            ModernAppCard(
                app = app,
                isPicked = isPicked,
                isOwnApp = isOwnApp,
                onPickApp = onPickApp,
                isLoading = isLoading
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ModernAppCard(
    app: App,
    isPicked: Boolean,
    isOwnApp: Boolean,
    onPickApp: (String) -> Unit,
    isLoading: Boolean = false
) {
    var appIconUrl by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Load app icon asynchronously
    LaunchedEffect(app.id) {
        try {
            appIconUrl = com.teksxt.closedtesting.util.TestSyncUtil.fetchAppIconUrl(app.id)
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // App Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon with shadow
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (appIconUrl != null) {
                        // Show app icon if available
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(appIconUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "App Icon for ${app.name}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(56.dp)
                                .padding(8.dp)
                        )
                    } else {
                        // Show first letter of app name in a colored circle if icon isn't available
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(getAppIconBackgroundColor(app.name)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.name.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    app.category?.let { category ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = "Category: $category",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Testers pill
//                    Surface(
//                        shape = RoundedCornerShape(16.dp),
//                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
//                        modifier = Modifier.padding(top = 8.dp)
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Outlined.Groups,
//                                contentDescription = "Testers: ${app.activeTesters}/${app.totalTesters}",
//                                modifier = Modifier.size(14.dp),
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            Text(
//                                text = "${app.activeTesters} testers",
//                                style = MaterialTheme.typography.labelMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
                }

                // Pick button or status
                PickButton(
                    isPicked = isPicked,
                    isOwnApp = isOwnApp,
                    onPick = { onPickApp(app.id) },
                    isLoading = isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = app.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test window pill
                Surface(
                    color = getTestWindowColor(app.testingDays.toString()),
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = app.testingDays.toString() +  " " + "days",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Play Store link
                    SmallIconButton(
                        icon = Icons.Outlined.Shop,
                        contentDescription = "Play Store",
                        onClick = {
                            openLink(context, app.playStoreUrl ?: "", "Play Store")
                        }
                    )

                    SmallIconButton(
                        icon = Icons.Outlined.Public,
                        contentDescription = "Web Join Link",
                        onClick = {
                            openLink(context, app.testApkUrl ?: "", "Web Join Link")
                        }
                    )

                    // Group link
                    SmallIconButton(
                        icon = Icons.Outlined.Group,
                        contentDescription = "Google Group",
                        onClick = {
                            openLink(context, app.testApkUrl ?: "", "Google Group")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PickButton(
    isPicked: Boolean,
    isOwnApp: Boolean,
    onPick: () -> Unit,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    when {
        isPicked -> {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Picked",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        isOwnApp -> {
            // New state: it's the user's own app
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.tertiary,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Your App",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        else -> {
            Button(
                onClick = onPick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                interactionSource = interactionSource,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    // Show spinner when loading
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Picking...",
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    // Normal state
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pick",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmallIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.size(36.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}



private fun openLink(context: Context, link: String, type: String) {
    if (link.isNotEmpty()) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open $type link",
                Toast.LENGTH_SHORT
            ).show()
        }
    } else {
        Toast.makeText(
            context,
            "No $type link available",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        EmptyContentView(
            title = "No Apps Found",
            description = "There are no apps available for testing at the moment. Check back later or try a different filter.",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun getTestWindowColor(testWindow: String): Color {
    return when {
        testWindow.contains("Today", ignoreCase = true) ->
            MaterialTheme.colorScheme.errorContainer
        testWindow.contains("Tomorrow", ignoreCase = true) ->
            MaterialTheme.colorScheme.tertiaryContainer
        testWindow.contains("Days", ignoreCase = true) ->
            MaterialTheme.colorScheme.secondaryContainer
        else ->
            MaterialTheme.colorScheme.surfaceVariant
    }
}

