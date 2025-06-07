package com.teksxt.closedtesting.core.presentation.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.teksxt.closedtesting.core.util.PermissionHandler

@Composable
fun StoragePermissionHandler(
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        if (PermissionHandler.hasStoragePermission(context)) {
            onPermissionResult(true)
        } else {
            permissionLauncher.launch(PermissionHandler.getStoragePermission())
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                onPermissionResult(false)
            },
            title = { Text("Permission Required") },
            text = { Text("Storage access is needed to select and share images in the chat.") },
            confirmButton = {
                Button(onClick = {
                    showRationale = false
                    permissionLauncher.launch(PermissionHandler.getStoragePermission())
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    onPermissionResult(false)
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}