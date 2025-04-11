package com.teksxt.closedtesting.presentation.navigation

sealed class Screen(val route: String) {


    // Onboarding
    object RoleSelection : Screen("role_selection")
    object ProfileSetup : Screen("profile_setup")
    object OnboardingComplete : Screen("onboarding_complete")

    // Dashboards based on user role
    object TesterDashboard : Screen("tester_dashboard")

    // Developer screens
    object RequestTesters : Screen("request_testers")
    object TestRequestDetail : Screen("test_request_detail")
    object TestersList : Screen("testers_list")

    // Tester screens
    object TestingGroups : Screen("testing_groups")
    object AvailableTests : Screen("available_tests")
    object TestDetail : Screen("test_detail")

    // Report screens
    object TestReport : Screen("test_report")
    object BugReport : Screen("bug_report")
    object ScreenshotUpload : Screen("screenshot_upload")

    // Subscription screens
    object Subscription : Screen("subscription")
    object Payment : Screen("payment")

    // Settings screens
    object Settings : Screen("settings")
    object ProfileEdit : Screen("profile_edit")
    object NotificationSettings : Screen("notification_settings")


    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object ForgotPassword : Screen("forgot_password")

    data object Dashboard : Screen("dashboard")

    data object MyRequests : Screen("my_requests")
    data object CreateRequest : Screen("create_request")
    data object DeveloperProfile : Screen("developer_profile")
    data object RequestDetails : Screen("request_details")
    data object RequestReports : Screen("request_reports")
}