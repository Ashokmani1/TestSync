package com.teksxt.closedtesting

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.teksxt.closedtesting.presentation.navigation.NavGraph
import com.teksxt.closedtesting.presentation.navigation.Screen
import com.teksxt.closedtesting.ui.theme.TestSyncTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.teksxt.closedtesting.util.TestSyncUtil.fetchAppIconUrl
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestSyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userSession by viewModel.userSession.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()
                    
                    if (isLoading) {
                        // Show loading indicator while checking authentication
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val navController = rememberNavController()
                        
                        // Determine start destination based on auth status
                        val startDestination = when {
                            userSession == null -> Screen.Login.route
                            else -> Screen.Dashboard.route
                        }
                        
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}