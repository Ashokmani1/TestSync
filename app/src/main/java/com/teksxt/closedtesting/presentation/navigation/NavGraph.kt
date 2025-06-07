package com.teksxt.closedtesting.presentation.navigation

import com.teksxt.closedtesting.presentation.onboarding.OnboardingScreen
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.teksxt.closedtesting.myrequest.presentation.create.premium.PremiumSupportScreen
import com.teksxt.closedtesting.myrequest.presentation.details.RequestDetailsScreen
import com.teksxt.closedtesting.myrequest.presentation.edit.EditRequestScreen
import com.teksxt.closedtesting.myrequest.presentation.list.MyRequestScreen
import com.teksxt.closedtesting.notifications.NotificationsScreen
import com.teksxt.closedtesting.picked.presentation.details.PickedAppDetailsScreen
import com.teksxt.closedtesting.picked.presentation.list.PickedAppListScreen
import com.teksxt.closedtesting.presentation.auth.EmailVerificationScreen
import com.teksxt.closedtesting.presentation.auth.ForgotPasswordScreen
import com.teksxt.closedtesting.presentation.auth.LoginScreen
import com.teksxt.closedtesting.presentation.auth.SignupScreen
import com.teksxt.closedtesting.presentation.help.HelpSupportScreen
import com.teksxt.closedtesting.presentation.help.PrivacyPolicyScreen
import com.teksxt.closedtesting.presentation.help.TermsConditionsScreen
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
        "chat", Screen.PremiumSupport.route, Screen.EmailVerification.route, Screen.Notifications.route
    )
    
    val shouldShowBottomBar = !outsideMainFlowRoutes.contains(currentRoute) && 
        currentRoute != "create_request" && currentRoute?.contains("request_details") != true &&
            currentRoute?.contains("assigned_users") != true && currentRoute?.startsWith("chat") != true
            && currentRoute?.startsWith("edit_request") != true && currentRoute?.startsWith(Screen.PrivacyPolicy.route) != true

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
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding(), bottom = if (shouldShowBottomBar) 0.dp else innerPadding.calculateBottomPadding())
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

            composable(
                route = Screen.EmailVerification.route
            ) {
                EmailVerificationScreen(
                    onBackToLogin = {
                        navController.navigateUp()
                    },
                    onContinue = {
                        // Navigate to home or onboarding
                        navController.navigate("my_requests") {
                            popUpTo(Screen.EmailVerification.route) { inclusive = true }
                        }
                    }
                )
            }

            // Auth routes
            composable(route = Screen.Login.route) {
                LoginScreen(
                    onNavigateToSignup = {
                        navController.navigate(Screen.Signup.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    onLoginSuccess = {
                        navController.navigate("my_requests") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    }
                )
            }


            composable(route = Screen.Signup.route) {
                SignupScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onSignupSuccess = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToTerms = {
                        navController.navigate(Screen.TermsConditions.route)
                    },
                    onNavigateToPrivacy = {
                        navController.navigate(Screen.PrivacyPolicy.route + "?isFromSignupFlow=true")
                    }
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
                    },
                    onNavigateToPremiumSupport = {
                        navController.navigate(Screen.PremiumSupport.route)
                    }
                )
            }

            composable(route = Screen.PremiumSupport.route) {
                val context = LocalContext.current
                PremiumSupportScreen(
                    onNavigateBack = { navController.navigateUp() },
                    onContactSupport = {
                        // Launch email intent or in-app support chat
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@testsync.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Premium Testing Service Inquiry")
                            putExtra(Intent.EXTRA_TEXT, "Hi, I'm interested in the premium testing service for my app.")
                        }
                        context.startActivity(intent)
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
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChatScreen = { requestID, ownerId, dayNumber ->
                        navController.navigate("chat/${requestID}/${ownerId}/${dayNumber}?isRequestRiser=false")
                    }
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
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToEmailVerificationScreen = {
                        navController.navigate(Screen.EmailVerification.route)
                    }
                )
            }

            // Help and legal screens
            composable(route = Screen.HelpSupport.route) {
                HelpSupportScreen(
                    onNavigateBack = { navController.popBackStack() }
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
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    isFromSignupFlow = isFromSignupFlow
                )
            }

            composable(
                route = "chat/{requestId}/{testerId}/{dayNumber}?isRequestRiser={isRequestRiser}",
                arguments = listOf(
                    navArgument("requestId") { type = NavType.StringType },
                    navArgument("testerId") { type = NavType.StringType },
                    navArgument("dayNumber") { type = NavType.IntType },
                    navArgument("isRequestRiser") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                val testerId = backStackEntry.arguments?.getString("testerId") ?: ""
                val dayNumber = backStackEntry.arguments?.getInt("dayNumber") ?: 1
                val isRequestRiser = backStackEntry.arguments?.getBoolean("isRequestRiser") ?: false

                ChatScreen(
                    testerId = testerId,
                    dayNumber = dayNumber,
                    isRequestRiser = isRequestRiser,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Notifications.route
            ) {
                NotificationsScreen(navController = navController)
            }
        }
    }
}