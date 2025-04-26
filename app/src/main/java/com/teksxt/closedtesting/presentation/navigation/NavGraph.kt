package com.teksxt.closedtesting.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.teksxt.closedtesting.chat.presentation.ChatScreen
import com.teksxt.closedtesting.explore.presentation.ExploreScreen
import com.teksxt.closedtesting.myrequest.presentation.create.CreateRequestScreen
import com.teksxt.closedtesting.myrequest.presentation.details.RequestDetailsScreen
import com.teksxt.closedtesting.myrequest.presentation.edit.EditRequestScreen
import com.teksxt.closedtesting.myrequest.presentation.list.MyRequestScreen
import com.teksxt.closedtesting.picked.presentation.details.PickedAppDetailsScreen
import com.teksxt.closedtesting.picked.presentation.list.PickedAppListScreen
import com.teksxt.closedtesting.presentation.auth.ForgotPasswordScreen
import com.teksxt.closedtesting.presentation.auth.LoginScreen
import com.teksxt.closedtesting.presentation.auth.SignupScreen
import com.teksxt.closedtesting.presentation.help.HelpSupportScreen
import com.teksxt.closedtesting.presentation.help.PrivacyPolicyScreen
import com.teksxt.closedtesting.presentation.help.TermsConditionsScreen
import com.teksxt.closedtesting.presentation.onboarding.OnboardingScreen
import com.teksxt.closedtesting.presentation.onboarding.OnboardingViewModel
import com.teksxt.closedtesting.presentation.splash.SplashScreen
import com.teksxt.closedtesting.settings.presentation.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "splash"
) {
    // Get current route for bottom nav visibility
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    // Determine when to show bottom nav
    val outsideMainFlowRoutes = listOf(
        "splash", 
        Screen.Login.route, 
        Screen.Signup.route, 
        Screen.ForgotPassword.route,
        Screen.Onboarding.route,
        Screen.HelpSupport.route,
        Screen.TermsConditions.route,
        Screen.PrivacyPolicy.route,
        "chat"
    )
    
    val shouldShowBottomBar = !outsideMainFlowRoutes.contains(currentRoute) && 
        currentRoute != "create_request" && currentRoute?.contains("request_details") != true &&
            currentRoute?.contains("assigned_users") != true && currentRoute?.startsWith("chat") != true && currentRoute?.startsWith("edit_request") != true

    // Remember the selected tab
    var selectedTab by remember { mutableIntStateOf(0) }

    // Create the Bottom Nav Scaffold
    BottomNavScaffold(
        navController = navController,
        shouldShowBottomBar = shouldShowBottomBar,
        selectedTabIndex = selectedTab,
        onTabSelected = {
            selectedTab = it
            when (it) {
                0 -> navController.navigate("my_requests") {
                    popUpTo("my_requests") {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                1 -> navController.navigate("picked_apps_list") {
                    popUpTo("my_requests") {
                        saveState = true  
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                2 -> navController.navigate("explore") {
                    popUpTo("my_requests") {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                3 -> navController.navigate(Screen.Settings.route) {
                    popUpTo("my_requests") {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        tabs = listOf(
            Triple(Icons.Default.List, "My Requests", "View your test requests"),
            Triple(Icons.Default.Task, "Assigned", "View assigned tests"),
            Triple(Icons.Default.Explore, "Explore", "Discover apps"),
            Triple(Icons.Default.Settings, "Settings", "Profile & settings")
        )
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash and auth flow
            composable("splash") {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate("my_requests") {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo("splash") { inclusive = true }
                        }
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            // Auth routes
            composable(route = Screen.Login.route) {
                LoginScreen(
                    onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = { 
                        navController.navigate("my_requests") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(route = Screen.Signup.route) {
                SignupScreen(
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onSignupSuccess = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    },
                    onNavigateToTerms = { navController.navigate(Screen.TermsConditions.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route + "?isFromSignupFlow=true") }
                )
            }
            
            composable(route = Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(route = Screen.Onboarding.route) {

                val onboardingViewModel: OnboardingViewModel = hiltViewModel()

                OnboardingScreen(
                    onComplete = {
                        onboardingViewModel.completeOnboarding()
                        navController.navigate("my_requests") {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    },
                    onSkip = {
                        onboardingViewModel.completeOnboarding()
                        navController.navigate("my_requests") {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // My Requests screens (from Dashboard)
            composable("my_requests") {

                MyRequestScreen(navController)
            }

            composable("create_request") {
                CreateRequestScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRequestCreated = { requestId ->
                        navController.navigate("my_requests") {
                            popUpTo("my_requests") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "edit_request/{requestId}",
                arguments = listOf(
                    navArgument("requestId") { type = NavType.StringType }
                )
            ) {
                val requestId = it.arguments?.getString("requestId") ?: ""
                EditRequestScreen(
                    requestId = requestId,
                    onNavigateBack = { navController.popBackStack() },
                    onRequestUpdated = { updatedRequestId ->
                        navController.popBackStack(
                            route = "my_requests", 
                            inclusive = false
                        )
                        navController.navigate("request_details/$updatedRequestId")
                    }
                )
            }

            composable(
                route = "request_details/{requestId}",
                arguments = listOf(
                    navArgument("requestId") { type = NavType.StringType }
                )
            ) {
                RequestDetailsScreen(
                    navController = navController,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("picked_apps_list") {
                PickedAppListScreen(
                    onNavigateToDetails = { pickedAppId ->
                        navController.navigate("picked_app_details/$pickedAppId")
                    }
                )
            }

            composable(
                "picked_app_details/{pickedAppId}",
                arguments = listOf(navArgument("pickedAppId") { type = NavType.StringType })
            ) {
                PickedAppDetailsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }


            // Explore screen (from Dashboard)
            composable("explore") {
                ExploreScreen()
            }

            // Settings and help screens
            composable(route = Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToHelp = { navController.navigate(Screen.HelpSupport.route) },
                    onNavigateToTerms = { navController.navigate(Screen.TermsConditions.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) }
                )
            }

            // Help and legal screens
            composable(route = Screen.HelpSupport.route) {
                HelpSupportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTerms = { navController.navigate(Screen.TermsConditions.route) },
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) }
                )
            }

            composable(route = Screen.TermsConditions.route) {
                TermsConditionsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.PrivacyPolicy.route + "?isFromSignupFlow={isFromSignupFlow}",
                arguments = listOf(
                    navArgument("isFromSignupFlow") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val isFromSignupFlow = backStackEntry.arguments?.getBoolean("isFromSignupFlow") ?: false
                PrivacyPolicyScreen(
                    onNavigateBack = { navController.popBackStack() },
                    isFromSignupFlow = isFromSignupFlow
                )
            }

            composable(
                route = "chat/{requestId}/{testerId}/{dayNumber}",
                arguments = listOf(
                    navArgument("requestId") { type = NavType.StringType },
                    navArgument("testerId") { type = NavType.StringType },
                    navArgument("dayNumber") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                val testerId = backStackEntry.arguments?.getString("testerId") ?: ""
                val dayNumber = backStackEntry.arguments?.getString("dayNumber")?.toIntOrNull() ?: 1

                ChatScreen(
                    requestId = requestId,
                    testerId = testerId,
                    dayNumber = dayNumber,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}