package com.teksxt.closedtesting.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<Triple<ImageVector, String, String>>
) {
    NavigationBar {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = tab.first,
                        contentDescription = tab.second
                    )
                },
                label = {
                    Text(
                        text = tab.second,
                        maxLines = 1
                    )
                },
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                alwaysShowLabel = true
            )
        }
    }
}