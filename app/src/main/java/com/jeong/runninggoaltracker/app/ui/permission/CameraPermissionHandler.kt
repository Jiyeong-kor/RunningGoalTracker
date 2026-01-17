package com.jeong.runninggoaltracker.app.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.jeong.runninggoaltracker.R

@Composable
fun CameraPermissionHandler(
    requestCameraPermission: (onResult: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    requestKey: Any = Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var permissionStatus by rememberSaveable { mutableStateOf(CameraPermissionStatus.Denied) }
    var hasRequested by rememberSaveable { mutableStateOf(false) }
    val showDialogState = rememberSaveable { mutableStateOf(false) }
    val activity = remember(context) { context.findActivity() }

    val confirmLabel = stringResource(R.string.camera_permission_allow)
    val settingsLabel = stringResource(R.string.camera_permission_settings)
    val cancelLabel = stringResource(R.string.camera_permission_cancel)
    val dialogTitle = stringResource(R.string.camera_permission_title)
    val dialogMessage = stringResource(R.string.camera_permission_message)
    val settingsMessage = stringResource(R.string.camera_permission_settings_message)

    fun refreshPermissionState(granted: Boolean) {
        if (granted) {
            permissionStatus = CameraPermissionStatus.Granted
            showDialogState.value = false
            return
        }
        val shouldShowRationale = activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
        } ?: false
        permissionStatus = if (!shouldShowRationale && hasRequested) {
            CameraPermissionStatus.PermanentlyDenied
        } else {
            CameraPermissionStatus.Denied
        }
        showDialogState.value = true
    }

    fun requestPermission() {
        requestCameraPermission { granted ->
            hasRequested = true
            refreshPermissionState(granted)
        }
    }

    fun isPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(requestKey) {
        if (isPermissionGranted()) {
            permissionStatus = CameraPermissionStatus.Granted
        } else {
            requestPermission()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (isPermissionGranted()) {
                    permissionStatus = CameraPermissionStatus.Granted
                    showDialogState.value = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showDialogState.value) {
        val isPermanentlyDenied = permissionStatus == CameraPermissionStatus.PermanentlyDenied
        AlertDialog(
            modifier = modifier,
            onDismissRequest = { showDialogState.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialogState.value = false
                        if (isPermanentlyDenied) {
                            context.openAppSettings()
                        } else {
                            requestPermission()
                        }
                    }
                ) {
                    Text(text = if (isPermanentlyDenied) settingsLabel else confirmLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogState.value = false }) {
                    Text(text = cancelLabel)
                }
            },
            title = { Text(text = dialogTitle) },
            text = { Text(text = if (isPermanentlyDenied) settingsMessage else dialogMessage) }
        )
    }
}

private enum class CameraPermissionStatus {
    Granted,
    Denied,
    PermanentlyDenied
}

private fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
