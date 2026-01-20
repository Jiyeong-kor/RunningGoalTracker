package com.jeong.runninggoaltracker.shared.designsystem.notification

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationPermissionGate {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS, conditional = true)
    fun notifyIfAllowed(
        context: Context,
        notificationId: Int,
        notification: Notification
    ): Boolean =
        if (!canPostNotifications(context)) {
            false
        } else {
            try {
                notifyBoundary(context, notificationId, notification)
                true
            } catch (_: SecurityException) {
                false
            }
        }

    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun notifyBoundary(
        context: Context,
        notificationId: Int,
        notification: Notification
    ) = NotificationManagerCompat.from(context).notify(notificationId, notification)
}
