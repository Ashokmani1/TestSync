package com.teksxt.closedtesting.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teksxt.closedtesting.assignedtests.details.AssignedTestDetailsScreen
import com.teksxt.closedtesting.assignedtests.list.AssignedTestsScreen
import com.teksxt.closedtesting.explore.presentation.ExploreScreen
import com.teksxt.closedtesting.myrequest.presentation.create.CreateRequestScreen
import com.teksxt.closedtesting.myrequest.presentation.list.MyRequestScreen
import com.teksxt.closedtesting.presentation.navigation.Screen
import com.teksxt.closedtesting.myrequest.presentation.details.RequestDetailsScreen
import com.teksxt.closedtesting.myrequest.presentation.details.RequestDetailsViewModel

import com.teksxt.closedtesting.myrequest.presentation.details.component.AssignedUsersScreen
import com.teksxt.closedtesting.ui.components.BottomNavBar
import com.teksxt.closedtesting.profile.ProfileScreen

@Composable
fun DeveloperDashboardScreen(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    println(currentRoute)
    val shouldShowBottomBar = currentRoute != "create_request" && currentRoute?.contains(Screen.RequestDetails.route) == false && !currentRoute.contains("assigned_users")

    var selectedTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar)
            {
                BottomNavBar(
                    selectedTabIndex = selectedTab,
                    onTabSelected = {
                        selectedTab = it
                        when (it) {
                            0 -> navController.navigate("my_requests") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            1 -> navController.navigate("assigned_tests") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            2 -> navController.navigate("explore") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            3 -> navController.navigate("developer_profile") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    tabs = listOf(
                        Triple(Icons.Default.List, "My Requests", "View your test requests"),
                        Triple(Icons.Default.Task, "Assigned", "Create a new test request"),
                        Triple(Icons.Default.Explore, "Explore", "View and edit your profile"),
                        Triple(Icons.Default.Settings, "Settings", "View and edit your profile")
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "my_requests",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("my_requests") {

                MyRequestScreen(navController)
            }

            composable("create_request") {

                CreateRequestScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRequestCreated = { requestId ->
                        // Navigate to request details or back to list
                        navController.navigate(Screen.MyRequests.route) {
                            popUpTo(Screen.MyRequests.route) { inclusive = true }
                        }
                    }
                )
            }

            composable("developer_profile") {
                ProfileScreen(
                    onNavigateToAuth = { navController.navigate("auth") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }},
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("explore") {

                ExploreScreen()
            }


            composable(
                route = Screen.RequestDetails.route + "/{requestId}",
                arguments = listOf(
                    navArgument("requestId") { type = NavType.StringType }
                )
            ) {
                RequestDetailsScreen(
                    navController = navController,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "assigned_users/all/{requestId}",
                arguments = listOf(
                    navArgument("requestId") { type = NavType.StringType }
                )
            ) {

                val viewModel: RequestDetailsViewModel = hiltViewModel()

                AssignedUsersScreen(
                    dayNumber = null, // Indicates viewing all testers
                    assignedTesters = viewModel.getAssignedTesters(null),
                    onNavigateBack = { navController.popBackStack() },
                    onSendReminder = { testerId ->
                        viewModel.sendReminder(null, testerId)
                    },
                    onSendBulkReminders = { testerIds ->
                        viewModel.sendBulkReminders(null, testerIds)
                    }
                )
            }

            composable(
                route = "assigned_users/{requestId}/{dayNumber}",
                arguments = listOf(
                    navArgument("requestId") { type = NavType.StringType },
                    navArgument("dayNumber") { type = NavType.IntType }
                )
            ) { backStackEntry ->

                val dayNumber = backStackEntry.arguments?.getInt("dayNumber") ?: 1

                val viewModel: RequestDetailsViewModel = hiltViewModel()

                AssignedUsersScreen(
                    dayNumber = dayNumber,
                    assignedTesters = viewModel.getAssignedTesters(dayNumber),
                    onNavigateBack = { navController.popBackStack() },
                    onSendReminder = { testerId ->
                        viewModel.sendReminder(dayNumber, testerId)
                    },
                    onSendBulkReminders = { testerIds ->
                        viewModel.sendBulkReminders(dayNumber, testerIds)
                    }
                )
            }

            composable("assigned_tests") {
                AssignedTestsScreen(
                    onNavigateToTestDetails = { testId ->
                        navController.navigate("assigned_tests/$testId")
                    }
                )
            }

            composable(
                route = "assigned_tests/{testId}",
                arguments = listOf(
                    navArgument("testId") { type = NavType.StringType }
                )
            ) {
                AssignedTestDetailsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}