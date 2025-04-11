package com.teksxt.closedtesting.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.teksxt.closedtesting.presentation.auth.ForgotPasswordScreen
import com.teksxt.closedtesting.presentation.auth.LoginScreen
import com.teksxt.closedtesting.presentation.auth.SignupScreen
import com.teksxt.closedtesting.presentation.common.DeveloperDashboardScreen
import com.teksxt.closedtesting.presentation.onboarding.OnboardingScreen
import com.teksxt.closedtesting.presentation.splash.SplashScreen


@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "splash"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Dashboard.route) {
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
                    // Check if onboarding is needed or navigate to appropriate dashboard
                    navController.navigate(Screen.Dashboard.route) {
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
                }
            )
        }
        
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Main dashboard
        composable(route = Screen.Dashboard.route) {
            DeveloperDashboardScreen(
//                onNavigateToMyRequests = { navController.navigate(Screen.MyRequests.route) },
//                onNavigateToMyTests = { navController.navigate(Screen.MyAssignedTests.route) },
//                onNavigateToExplore = { navController.navigate(Screen.ExploreApps.route) },
//                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
        
//        // My Requests screens
//        composable(route = Screen.MyRequests.route) {
//            MyRequestsScreen(
//                onNavigateBack = { navController.popBackStack() },
//                onNavigateToCreateRequest = { navController.navigate(Screen.CreateRequest.route) },
//                onNavigateToRequestDetails = { requestId ->
//                    navController.navigate(Screen.RequestDetails.createRoute(requestId))
//                }
//            )
//        }
//
//        composable(route = Screen.CreateRequest.route) {
//            CreateRequestScreen(
//                onNavigateBack = { navController.popBackStack() },
//                onRequestCreated = {
//                    navController.navigate(Screen.MyRequests.route) {
//                        popUpTo(Screen.MyRequests.route) { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        composable(
//            route = Screen.RequestDetails.route,
//            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
//            RequestDetailsScreen(
//                requestId = requestId,
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        // My Assigned Tests screens
//        composable(route = Screen.MyAssignedTests.route) {
//            MyAssignedTestsScreen(
//                onNavigateBack = { navController.popBackStack() },
//                onNavigateToTestDetails = { testId ->
//                    navController.navigate(Screen.AssignedTestDetails.createRoute(testId))
//                }
//            )
//        }
//
//        composable(
//            route = Screen.AssignedTestDetails.route,
//            arguments = listOf(navArgument("testId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val testId = backStackEntry.arguments?.getString("testId") ?: ""
//            AssignedTestDetailsScreen(
//                testId = testId,
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        // Explore screen
//        composable(route = Screen.ExploreApps.route) {
//            ExploreAppsScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        // User related screens
//        composable(route = Screen.Profile.route) {
//            ProfileScreen(
//                onNavigateBack = { navController.popBackStack() },
//                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
//            )
//        }
//
//        composable(route = Screen.Notifications.route) {
//            NotificationsScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        composable(route = Screen.Settings.route) {
//            SettingsScreen(
//                onNavigateBack = { navController.popBackStack() },
//                onNavigateToHelpSupport = { navController.navigate(Screen.HelpSupport.route) },
//                onNavigateToTerms = { navController.navigate(Screen.TermsConditions.route) },
//                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) }
//            )
//        }
//
//        // Help and legal screens
//        composable(route = Screen.HelpSupport.route) {
//            HelpSupportScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        composable(route = Screen.TermsConditions.route) {
//            TermsConditionsScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        composable(route = Screen.PrivacyPolicy.route) {
//            PrivacyPolicyScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        composable(route = Screen.DeveloperProfile.route) {
//            // Temporarily reusing the ProfileScreen here
//            ProfileScreen(
//                onNavigateBack = { navController.popBackStack() },
//                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
//            )
//        }
    }
}