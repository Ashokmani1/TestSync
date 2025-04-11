package com.teksxt.closedtesting.presentation.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teksxt.closedtesting.presentation.subscription.components.PlanCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateToPayment: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Define subscription plans
    val subscriptionPlans = listOf(
        SubscriptionPlan(
            title = "Free",
            price = "$0/month",
            features = listOf(
                "Up to 3 active tests",
                "Basic bug reporting",
                "Limited test groups"
            ),
            type = "FREE"
        ),
        SubscriptionPlan(
            title = "Basic",
            price = "$9.99/month",
            features = listOf(
                "Up to 10 active tests",
                "Advanced bug reporting",
                "File attachments",
                "Priority matching"
            ),
            type = "BASIC"
        ),
        SubscriptionPlan(
            title = "Premium",
            price = "$19.99/month",
            features = listOf(
                "Unlimited active tests",
                "Advanced bug reporting",
                "File attachments",
                "Priority matching",
                "Premium support",
                "Analytics dashboard",
                "Custom tester recruitment"
            ),
            type = "PREMIUM"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription Plans") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current subscription info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Current Subscription",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp)
                            )
                        } else if (uiState.subscription != null) {
                            val subscription = uiState.subscription!!
                            Text(
                                text = "Plan: ${subscription.planType}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Status: ${if (subscription.isActive) "Active" else "Inactive"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Expires: ${dateFormatter.format(subscription.endDate)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "No active subscription",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Select a plan below to subscribe",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Subscription plans
            item {
                Text(
                    text = "Available Plans",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(subscriptionPlans) { plan ->
//                uiState.subscription?.planType == plan.type
                val isCurrentPlan = true
                PlanCard(
                    plan = plan,
                    isCurrentPlan = isCurrentPlan,
                    onClick = { 
                        if (!isCurrentPlan) {
                            onNavigateToPayment(plan.type)
                        }
                    }
                )
            }

            // Error message
            if (uiState.error != null) {
                item {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

data class SubscriptionPlan(
    val title: String,
    val price: String,
    val features: List<String>,
    val type: String
)