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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.core.presentation.component.NotificationPermissionHandler
import com.teksxt.closedtesting.presentation.navigation.NavGraph
import com.teksxt.closedtesting.ui.theme.TestSyncTheme
import com.teksxt.closedtesting.util.PermissionHandler
import dagger.hilt.android.AndroidEntryPoint
import java.util.TimeZone
import kotlin.text.compareTo
import java.text.*;
import java.util.Calendar;
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

//        FirebaseFirestore.getInstance()
//            .collection("users")
//            .get()
//            .addOnSuccessListener { documents ->
//                for (document in documents) {
//                    val timestamp = document.getTimestamp("lastActive")
//
//                    if (timestamp != null) {
//                        // Format local time
//                        val localFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                        localFormat.timeZone = TimeZone.getDefault()
//                        val localTime = localFormat.format(timestamp.toDate())
//
//                        // Format UTC time
//                        val utcFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                        utcFormat.timeZone = TimeZone.getTimeZone("UTC")
//                        val utcTime = utcFormat.format(timestamp.toDate())
//
//                        Log.d("Timestamps", "Local Time: $localTime")
//                        Log.d("Timestamps", "UTC Time: $utcTime")
//                    } else {
//                        Log.w("Timestamps", "Timestamp is null (maybe still syncing from server)")
//                    }
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.w("Firestore", "Error getting documents", e)
//            }
        setContent {

            TestSyncTheme {
                // Get context for permission checking
                val context = LocalContext.current

                var shouldShowPermissionDialog by remember {
                    mutableStateOf(false)
                }

                // Handle notification permission check after splash screen
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        shouldShowPermissionDialog = !PermissionHandler.hasNotificationPermission(context)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Main navigation graph
                    NavGraph(
                        navController = navController,
                        startDestination = "splash"
                    )

                    if (shouldShowPermissionDialog) {
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
        val channel = NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Default notification channel"
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}