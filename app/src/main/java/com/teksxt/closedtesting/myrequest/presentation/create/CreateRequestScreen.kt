package com.teksxt.closedtesting.myrequest.presentation.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teksxt.closedtesting.myrequest.presentation.create.premium.PremiumRequestBanner
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    onNavigateBack: () -> Unit,
    onRequestCreated: (String) -> Unit,
    onNavigateToPremiumSupport: () -> Unit,
    viewModel: CreateRequestViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val isLoading by viewModel.isLoading.collectAsState()
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
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)) {
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
                .imePadding()
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

                // Premium Support Banner
                PremiumRequestBanner {

                    onNavigateToPremiumSupport()
                }

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

                OutlinedTextField(
                    value = viewModel.joinWebLink,
                    onValueChange = { viewModel.updateJoinWebLink(it) },
                    label = { Text("Testing Join Link") },
                    placeholder = { Text("https://play.google.com/apps/testing/...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    isError = viewModel.joinWebLinkError != null,
                    supportingText = viewModel.joinWebLinkError?.let { {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    } } ?: { Text("Link for testers to join testing program") },
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

                ExposedDropdownMenuBox(
                    expanded = viewModel.isDropdownExpanded,
                    onExpandedChange = { viewModel.setCategoryDropdownExpanded(it) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = viewModel.appCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("App Category") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = viewModel.isDropdownExpanded)
                        },
                        isError = viewModel.appCategoryError != null,
                        supportingText = viewModel.appCategoryError?.let { {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = viewModel.isDropdownExpanded,
                        onDismissRequest = { viewModel.setCategoryDropdownExpanded(false) },
                        modifier = Modifier.exposedDropdownSize()
                    ) {
                        viewModel.appCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    viewModel.updateAppCategory(category)
                                    viewModel.setCategoryDropdownExpanded(false)
                                }
                            )
                        }
                    }
                }

                // Number of Testers
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
                    } } ?: { Text("Max 20 days") },
                    modifier = Modifier.fillMaxWidth(),
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Add leading icon to match other form fields
                        Icon(
                            Icons.Default.Redeem,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        // Use weight to push switch to the end
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Premium Application",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Toggle if your app includes in-app purchases or paid features",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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

                AnimatedVisibility(visible = viewModel.isPremium) {
                    OutlinedTextField(
                        value = viewModel.promoCode,
                        onValueChange = { viewModel.updatePromoCode(it) },
                        label = { Text("Premium Promo Code") },
                        placeholder = { Text("Enter promo code for testers") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Redeem,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        isError = viewModel.promoCodeError != null,
                        supportingText = viewModel.promoCodeError?.let { {
                            Text(it, color = MaterialTheme.colorScheme.error)
                        } } ?: { Text("Premium app purchase promo code") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        )
                    )
                }

                OutlinedTextField(
                    value = viewModel.testingInstructions,
                    onValueChange = { viewModel.updateTestingInstructions(it) },
                    label = { Text("Testing Instructions") },
                    placeholder = { Text("Provide steps for testing or applying promo code") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    supportingText = { Text("Steps to apply promo or test your app's features") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )

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