package com.teksxt.closedtesting.myrequest.presentation.details

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.presentation.details.component.AppHeaderCard
import com.teksxt.closedtesting.myrequest.presentation.details.component.ModernTestingDaysAndTesterSection
import com.teksxt.closedtesting.myrequest.presentation.details.component.QuickActionButtons
import kotlinx.coroutines.flow.collectLatest
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RequestDetailsScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    viewModel: RequestDetailsViewModel = hiltViewModel()
) {
    val request by viewModel.request.collectAsState()
    val appDetails by viewModel.appDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Scroll state
    val scrollState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Calculate animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000)
    )

    LaunchedEffect(Unit) {
        viewModel.scrollToPosition.collectLatest { position ->
            // Scroll to the specified position with animation
            scrollState.animateScrollToItem(position)
        }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { errorMessage ->
            snackbarHostState.showSnackbar(message = errorMessage, duration = SnackbarDuration.Short)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    // Handle navigation events (NEW)
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { navigationEvent ->
            when (navigationEvent) {
                is NavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is NavigationEvent.NavigateToEditRequest -> {
                    navController.navigate("edit_request/${navigationEvent.requestId}")
                }
            }
        }
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Request") },
            text = { Text("Are you sure you want to delete this request? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppDetailsTopBar(
                title = request?.title ?: "",
                onBackClick = onNavigateBack,
                onEditClick = { viewModel.editRequest() },
                onDeleteClick = { showDeleteDialog = true },
                onShareClick = { viewModel.shareAppDetails(context) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        if (isLoading && request == null) {
            LoadingContent(modifier = Modifier.padding(paddingValues))
        } else if (request != null) {
            MainContent(
                viewModel = viewModel,
                request = request!!,
                selectedDay = selectedDay,
                progress = animatedProgress,
                scrollState = scrollState,
                onDaySelected = viewModel::setSelectedDay,
                onViewPlayStore = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${request!!.appId}".toUri())
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Couldn't open Play Store", Toast.LENGTH_SHORT).show()
                    }
                },
                onGoogleGroupUrlClick = {
                   try {
                       val intent = Intent(Intent.ACTION_VIEW, appDetails?.googleGroupUrl?.toUri())
                       context.startActivity(intent)
                   } catch (e: Exception) {
                       Toast.makeText(context, "Couldn't open Play Store", Toast.LENGTH_SHORT).show()
                   }
                },
                onViewTesters = {
                    if ((request?.testingDays ?: 0) > 0) {
                        // Scroll to testers section (index 2, after app card and buttons)
                        viewModel.scrollToTesters()

                    } else {
                        // Show a message if there are no testing days configured
                        Toast.makeText(context, "No testers available for this request", Toast.LENGTH_SHORT).show()
                    }
                },
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            ErrorContent(
                onRetry = { viewModel.refreshData() },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDetailsTopBar(
    title: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            var showMenu by remember { mutableStateOf(false) }

            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Request") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    onClick = {
                        showMenu = false
                        onEditClick()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Share Request") },
                    leadingIcon = { Icon(Icons.Default.Share, null) },
                    onClick = {
                        showMenu = false
                        onShareClick()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Delete Request", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showMenu = false
                        onDeleteClick()
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        windowInsets = WindowInsets(0, 0, 0, 0)
    )
}

@Composable
private fun MainContent(
    viewModel: RequestDetailsViewModel,
    request: Request,
    selectedDay: Int,
    progress: Float,
    scrollState: LazyListState,
    onDaySelected: (Int) -> Unit,
    onViewPlayStore: () -> Unit,
    onViewTesters: () -> Unit,
    onGoogleGroupUrlClick: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = scrollState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            // App details card
            AppHeaderCard(
                request = request,
                progress = progress,
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            // Quick action buttons
            QuickActionButtons(
                onPlayStoreClick = onViewPlayStore,
                onTestersClick = onViewTesters,
                onGoogleGroupUrlClick = onGoogleGroupUrlClick,
                testingDays = request.testingDays,
                testerCount = "${request.currentTestersCount}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Only show testing days section if there are testing days
        if (request.testingDays > 0) {

            item {
                ModernTestingDaysAndTesterSection(
                    viewModel = viewModel,
                    request = request,
                    selectedDay = selectedDay,
                    onDaySelected = onDaySelected,
                    navController = navController,
                    onSendBulkReminder = { dayNumber ->
                        viewModel.sendBulkReminders(selectedDay)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Failed to load request details",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "There was a problem loading the request details. Please try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
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


// Define a enum for day status
enum class DayStatus {
    Completed,
    InProgress,
    Upcoming,
    Expired
}