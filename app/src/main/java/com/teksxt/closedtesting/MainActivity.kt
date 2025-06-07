package com.teksxt.closedtesting

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.core.presentation.component.NotificationPermissionHandler
import com.teksxt.closedtesting.core.presentation.viewmodel.AppViewModel
import com.teksxt.closedtesting.core.theme.ThemeManager
import com.teksxt.closedtesting.presentation.navigation.NavGraph
import com.teksxt.closedtesting.ui.theme.TestSyncTheme
import com.teksxt.closedtesting.core.util.PermissionHandler
import dagger.hilt.android.AndroidEntryPoint
import java.util.TimeZone
import kotlin.text.compareTo
import java.text.*;
import java.util.Calendar;
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {

            val appState by viewModel.appState.collectAsState()

            TestSyncTheme(darkTheme = ThemeManager.isDarkTheme(appState.themeMode)) {
                // Get context for permission checking
                val context = LocalContext.current

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route
                
                    var shouldShowPermissionDialog by remember { mutableStateOf(false) }
                    
                    // Only check for permissions when we reach my_requests screen
                    LaunchedEffect(currentRoute) {
                        if (currentRoute == "my_requests" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            shouldShowPermissionDialog = !PermissionHandler.hasNotificationPermission(context)
                        }
                    }

                    // Main navigation graph
                    NavGraph(
                        navController = navController,
                        startDestination = "splash"
                    )

                    if (shouldShowPermissionDialog && currentRoute == "my_requests") {
                        NotificationPermissionHandler(
                            shouldRequestPermission = true,
                            onPermissionResult = { granted ->
                                shouldShowPermissionDialog = false
                                if (granted) {
                                    createNotificationChannel()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun createNotificationChannel()
    {
        val channel = NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Default notification channel"
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}