package com.teksxt.closedtesting.myrequest.presentation.details.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignedUsersScreen(
    dayNumber: Int?,
    assignedTesters: List<AssignedTester>,
    onNavigateBack: () -> Unit,
    onSendReminder: (String) -> Unit,
    onSendBulkReminders: (List<String>) -> Unit // Add new parameter
) {
    var selectedFilter by remember { mutableStateOf(TesterFilter.ALL) }
    var selectedTesters by remember { mutableStateOf(setOf<String>()) }
    var isSelectMode by remember { mutableStateOf(false) }

    val filteredTesters = remember(selectedFilter, assignedTesters) {
        when (selectedFilter) {
            TesterFilter.ALL       -> assignedTesters
            TesterFilter.COMPLETED -> assignedTesters.filter { it.hasCompleted }
            TesterFilter.PENDING   -> assignedTesters.filter { !it.hasCompleted }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectMode) {
                        Text("${selectedTesters.size} Selected")
                    } else {
                        Text(if (dayNumber == null) "Testers" else "Day $dayNumber Testers")
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = if (isSelectMode) {
                            {
                                isSelectMode = false
                                selectedTesters = emptySet()
                            }
                        } else {
                            onNavigateBack
                        }
                    ) {
                        Icon(
                            if (isSelectMode) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = if (isSelectMode) "Cancel" else "Back"
                        )
                    }
                },
                actions = {
                    if (isSelectMode && selectedTesters.isNotEmpty()) {
                        // Send bulk reminders action
                        IconButton(
                            onClick = {
                                onSendBulkReminders(selectedTesters.toList())
                                isSelectMode = false
                                selectedTesters = emptySet()
                            }
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send reminders"
                            )
                        }
                    } else if (!isSelectMode) {
                        // Select mode toggle
                        IconButton(
                            onClick = { isSelectMode = true }
                        ) {
                            Icon(
                                Icons.Outlined.SelectAll,
                                contentDescription = "Select testers"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filter chips
            item {
                FilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    testers = assignedTesters
                )
            }

            // List of testers
            items(
                items = filteredTesters,
                key = { it.id }
            ) { tester ->
                AssignedUserItem(
                    tester = tester,
                    isSelected = selectedTesters.contains(tester.id),
                    isSelectMode = isSelectMode,
                    onSendReminder = onSendReminder,
                    onToggleSelection = { isSelected ->
                        selectedTesters = if (isSelected) {
                            selectedTesters + tester.id
                        } else {
                            selectedTesters - tester.id
                        }
                    },
                    onLongPress = {
                        if (!tester.hasCompleted && !isSelectMode) {
                            isSelectMode = true
                            selectedTesters = setOf(tester.id)
                        }
                    }
                )
            }

            // Empty state
            if (filteredTesters.isEmpty()) {
                item {
                    EmptyFilterState(filter = selectedFilter)
                }
            }
        }
    }
}

enum class TesterFilter {
    ALL,
    COMPLETED,
    PENDING
}


@Composable
private fun FilterChips(
    selectedFilter: TesterFilter,
    onFilterSelected: (TesterFilter) -> Unit,
    testers: List<AssignedTester>
) {
    val completedCount = testers.count { it.hasCompleted }
    val pendingCount = testers.size - completedCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == TesterFilter.ALL,
            onClick = { onFilterSelected(TesterFilter.ALL) },
            label = { Text("All (${testers.size})") },
            leadingIcon = {
                Icon(
                    imageVector = if (selectedFilter == TesterFilter.ALL)
                        Icons.Filled.Check
                    else
                        Icons.Outlined.Group,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )

        FilterChip(
            selected = selectedFilter == TesterFilter.COMPLETED,
            onClick = { onFilterSelected(TesterFilter.COMPLETED) },
            label = { Text("Completed ($completedCount)") },
            leadingIcon = {
                Icon(
                    imageVector = if (selectedFilter == TesterFilter.COMPLETED)
                        Icons.Filled.Check
                    else
                        Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        FilterChip(
            selected = selectedFilter == TesterFilter.PENDING,
            onClick = { onFilterSelected(TesterFilter.PENDING) },
            label = { Text("Pending ($pendingCount)") },
            leadingIcon = {
                Icon(
                    imageVector = if (selectedFilter == TesterFilter.PENDING)
                        Icons.Filled.Check
                    else
                        Icons.Outlined.Pending,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
            )
        )
    }
}

@Composable
private fun EmptyFilterState(filter: TesterFilter) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when(filter) {
                    TesterFilter.COMPLETED -> Icons.Outlined.CheckCircle
                    TesterFilter.PENDING   -> Icons.Outlined.Pending
                    TesterFilter.ALL       -> Icons.Outlined.Group
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when(filter) {
                    TesterFilter.COMPLETED -> "No completed tests yet"
                    TesterFilter.PENDING   -> "No pending tests"
                    TesterFilter.ALL       -> "No testers assigned"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssignedUserItem(
    tester: AssignedTester,
    isSelected: Boolean,
    isSelectMode: Boolean,
    onSendReminder: (String) -> Unit,
    onToggleSelection: (Boolean) -> Unit,
    onLongPress: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                tester.hasCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier.combinedClickable(
            enabled = !tester.hasCompleted,
            onClick = {
                if (isSelectMode) {
                    onToggleSelection(!isSelected)
                }
            },
            onLongClick = onLongPress
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectMode && !tester.hasCompleted) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onToggleSelection,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                }
                else
                {
                    // Avatar
                    Surface(
                        shape = CircleShape,
                        color = if (tester.hasCompleted)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tester.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tester.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = tester.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!tester.hasCompleted && !isSelectMode) {
                    IconButton(
                        onClick = { onSendReminder(tester.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send reminder",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Testing status
            if (tester.hasCompleted) {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Show screenshot if available
                    if (tester.screenshotUrl != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Screenshot submitted",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Show feedback if available
                    if (tester.feedback != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Feedback provided",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}