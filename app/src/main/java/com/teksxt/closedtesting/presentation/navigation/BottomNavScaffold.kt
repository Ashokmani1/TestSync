package com.teksxt.closedtesting.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

/**
 * A scaffold with bottom navigation that controls when to show the bottom bar
 */
@Composable
fun BottomNavScaffold(
    navController: NavHostController,
    shouldShowBottomBar: Boolean,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<Triple<ImageVector, String, String>>,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavBar(
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = onTabSelected,
                    tabs = tabs
                )
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}