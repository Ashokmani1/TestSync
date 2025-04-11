package com.teksxt.closedtesting.myrequest.presentation.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    onNavigateBack: () -> Unit,
    onRequestCreated: (String) -> Unit,
    viewModel: CreateRequestViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.createSuccess.collectLatest { requestId ->
            onRequestCreated(requestId)
        }

        viewModel.errorMessage.collectLatest { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Test Request") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                // Submit Button
                Button(
                    onClick = { viewModel.submitRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(48.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "Submit Request",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(scrollState)
                    .alpha(if (isLoading) 0.6f else 1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // App Name
                OutlinedTextField(
                    value = viewModel.appName,
                    onValueChange = { viewModel.updateAppName(it) },
                    label = { Text("App Name") },
                    placeholder = { Text("Enter your app name") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Apps, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    singleLine = true,
                    isError = viewModel.appNameError != null,
                    supportingText = viewModel.appNameError?.let { { 
                        Text(it, color = MaterialTheme.colorScheme.error) 
                    } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )

                // Description
                OutlinedTextField(
                    value = viewModel.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description") },
                    placeholder = { Text("Describe your app") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Description, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    minLines = 3,
                    maxLines = 5,
                    isError = viewModel.descriptionError != null,
                    supportingText = viewModel.descriptionError?.let { { 
                        Text(it, color = MaterialTheme.colorScheme.error) 
                    } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )

                // Google Group Link
                OutlinedTextField(
                    value = viewModel.groupLink,
                    onValueChange = { viewModel.updateGroupLink(it) },
                    label = { Text("Google Group Link") },
                    placeholder = { Text("https://groups.google.com/...") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Group, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    singleLine = true,
                    isError = viewModel.groupLinkError != null,
                    supportingText = viewModel.groupLinkError?.let { { 
                        Text(it, color = MaterialTheme.colorScheme.error) 
                    } } ?: { Text("Link where testers can join the group") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )

                // Play Store Link
                OutlinedTextField(
                    value = viewModel.playStoreLink,
                    onValueChange = { viewModel.updatePlayStoreLink(it) },
                    label = { Text("Play Store Link") },
                    placeholder = { Text("https://play.google.com/store/apps/...") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Link, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    singleLine = true,
                    isError = viewModel.playStoreLinkError != null,
                    supportingText = viewModel.playStoreLinkError?.let { { 
                        Text(it, color = MaterialTheme.colorScheme.error) 
                    } } ?: { Text("Link to your app on Google Play") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Number of Testers
                    OutlinedTextField(
                        value = viewModel.numberOfTesters,
                        onValueChange = { viewModel.updateNumberOfTesters(it) },
                        label = { Text("Number of Testers") },
                        placeholder = { Text("10") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.People, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        singleLine = true,
                        isError = viewModel.numberOfTestersError != null,
                        supportingText = viewModel.numberOfTestersError?.let { { 
                            Text(it, color = MaterialTheme.colorScheme.error) 
                        } } ?: { Text("Max 20 testers") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        )
                    )

                    // Duration in Days
                    OutlinedTextField(
                        value = viewModel.durationInDays,
                        onValueChange = { viewModel.updateDurationInDays(it) },
                        label = { Text("Duration (Days)") },
                        placeholder = { Text("14") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.DateRange, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        singleLine = true,
                        isError = viewModel.durationInDaysError != null,
                        supportingText = viewModel.durationInDaysError?.let { { 
                            Text(it, color = MaterialTheme.colorScheme.error) 
                        } } ?: { Text("Max 28 days") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        )
                    )
                }

                // Premium Switch
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Premium Request",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "Get priority access to testers",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = viewModel.isPremium,
                            onCheckedChange = { viewModel.updateIsPremium(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }

                // Free Request Note
                AnimatedVisibility(visible = !viewModel.isPremium) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Text(
                            "For free requests, you must test 15 apps from other developers.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }


                // Add some space at the bottom for better scrolling experience
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Loading overlay with scrim
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Creating request...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}