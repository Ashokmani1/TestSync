package com.teksxt.closedtesting.picked.presentation.list

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.teksxt.closedtesting.R
import com.teksxt.closedtesting.core.presentation.component.AnimatedPreloader
import com.teksxt.closedtesting.core.presentation.component.getAppIconBackgroundColor
import com.teksxt.closedtesting.picked.presentation.list.component.PickedAppSearchBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickedAppListScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: PickedAppListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                PickedAppSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    isSearchExpanded = isSearchExpanded,
                    onSearchExpandChange = { isSearchExpanded = it },
                    selectedFilter = selectedFilter,
                    onFilterSelected = viewModel::onFilterSelected
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                PullToRefreshBox(
                    isRefreshing = state.isLoading && state.pickedApps.isNotEmpty(),
                    onRefresh = { viewModel.loadPickedApps() },
                    state = pullRefreshState
                ) {
                    if (state.isLoading && state.pickedApps.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (state.error != null && state.pickedApps.isEmpty()) {
                        ErrorView(
                            error = state.error!!,
                            onRetry = { viewModel.loadPickedApps() }
                        )
                    } else if (state.pickedApps.isEmpty()) {
                        EmptyFilterResultView(
                            isSearching = searchQuery.isNotEmpty() || selectedFilter != PickedAppFilter.ALL,
                            onClearFilters = {
                                viewModel.onSearchQueryChange("")
                                viewModel.onFilterSelected(PickedAppFilter.ALL)
                                isSearchExpanded = false
                            }
                        )
                    } else {
                        PickedAppsList(
                            pickedApps = state.pickedApps,
                            onItemClick = onNavigateToDetails,
                            onTogglePinned = { viewModel.togglePinnedStatus(it) },
                            isLoading = state.isLoading,
                            listState = listState
                        )
                    }
                }

                // Scroll to top FAB
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
    }
}

@Composable
fun EmptyFilterResultView(
    isSearching: Boolean,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSearching) {

            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No matching apps found",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try adjusting your search or filters to find what you're looking for",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            ElevatedButton(onClick = onClearFilters) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Filters")
            }
        } else {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Box {
                    AnimatedPreloader(R.raw.my_assigned_empty, modifier = Modifier.size(200.dp).align(Alignment.Center))
                }

                Text(
                    text = "No apps picked yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Go to Explore to pick apps for testing", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
                )
            }
        }
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
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        ElevatedButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Composable
fun PickedAppsList(
    pickedApps: List<PickedAppWithDetails>,
    onItemClick: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(
            items = pickedApps,
            key = { _, item -> item.id }
        ) { index, pickedApp ->
            ModernPickedAppItem(
                pickedApp = pickedApp,
                onItemClick = { onItemClick(pickedApp.id) },
                onTogglePinned = { onTogglePinned(pickedApp.id) },
                animationDelay = index * 50
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Floating loading indicator
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Updating...",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}


@Composable
fun ModernPickedAppItem(
    pickedApp: PickedAppWithDetails,
    onItemClick: () -> Unit,
    onTogglePinned: () -> Unit,
    animationDelay: Int = 0
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(initialAlpha = 0.3f, animationSpec = tween(300))
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onItemClick),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status indicator bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(getStatusColor(pickedApp.status))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App icon
                    AppIcon(
                        iconUrl = pickedApp.iconUrl,
                        appName = pickedApp.name,
                        status = pickedApp.status
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // App details
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = pickedApp.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = pickedApp.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress section
                        LinearProgressIndicator(
                            progress = pickedApp.completionRate,
                            modifier = Modifier.fillMaxWidth(),
                            color = getProgressColor(pickedApp.completionRate)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Day ${pickedApp.currentTestDay}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "${(pickedApp.completionRate * 100).toInt()}% complete",
                                style = MaterialTheme.typography.labelMedium,
                                color = getProgressColor(pickedApp.completionRate)
                            )
                        }
                    }

                    // Action buttons
                    IconButton(onClick = onTogglePinned) {
                        Icon(
                            imageVector = if (pickedApp.isPinned) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (pickedApp.isPinned) "Unpin app" else "Pin app",
                            tint = if (pickedApp.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tester count and category info
                if (pickedApp.app != null) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
//                        // Category chip
//                        pickedApp.category?.let { category ->
//                            SuggestionChip(
//                                onClick = { },
//                                label = { Text(category) },
//                                modifier = Modifier.padding(end = 8.dp),
//                                icon = {
//                                    Icon(
//                                        Icons.Default.Category,
//                                        contentDescription = null,
//                                        modifier = Modifier.size(18.dp)
//                                    )
//                                }
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.weight(1f))

                        // Testers info
                        val activeTesters = pickedApp.activeTesters ?: 0
                        val totalTesters = pickedApp.totalTesters ?: 0

                        Text(
                            text = "$activeTesters/$totalTesters testers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppIcon(iconUrl: String?, appName: String, status: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (iconUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(iconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = appName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getAppIconBackgroundColor(appName)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
        }

        // Status indicator badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(getStatusColor(status)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getStatusIcon(status),
                contentDescription = status,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "ACTIVE" -> MaterialTheme.colorScheme.primary
        "COMPLETED" -> Color(0xFF4CAF50) // Green
        "ABANDONED" -> Color(0xFFF44336) // Red
        else -> MaterialTheme.colorScheme.secondary
    }
}

@Composable
fun getProgressColor(progress: Float): Color {
    return when {
        progress >= 0.75f -> Color(0xFF4CAF50) // Green
        progress >= 0.5f -> Color(0xFF2196F3) // Blue
        progress >= 0.25f -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

@Composable
fun getStatusIcon(status: String) = when (status.uppercase()) {
    "ACTIVE" -> Icons.Outlined.PendingActions
    "COMPLETED" -> Icons.Outlined.CheckCircle
    else -> Icons.Default.Info
}