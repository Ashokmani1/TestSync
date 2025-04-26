package com.teksxt.closedtesting.presentation.navigation

sealed class Screen(val route: String) {
    
    // Auth screens
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object ForgotPassword : Screen("forgot_password")
    data object Onboarding : Screen("onboarding")

    // Main
    data object Dashboard : Screen("dashboard")

    // My Requests
    data object MyRequests : Screen("my_requests")
    data object CreateRequest : Screen("create_request")
    data object RequestDetails : Screen("request_details")
    data object RequestReports : Screen("request_reports")
    
    // Assigned Tests
    data object MyAssignedTests : Screen("my_assigned_tests")
    data object AssignedTestDetails : Screen("assigned_test_details/{testId}") {
        fun createRoute(testId: String) = "assigned_test_details/$testId"
    }
    
    // Explore
    data object ExploreApps : Screen("explore_apps")
    
    // User related
    data object DeveloperProfile : Screen("developer_profile")
    data object Profile : Screen("profile")
    data object Notifications : Screen("notifications")
    data object Settings : Screen("settings")
    
    // Help and Support
    data object HelpSupport : Screen("help_support")
    data object TermsConditions : Screen("terms_conditions")
    data object PrivacyPolicy : Screen("privacy_policy")
}