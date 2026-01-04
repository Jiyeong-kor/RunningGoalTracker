package com.jeong.runninggoaltracker.feature.record.tracking

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.content.pm.PackageManager
import com.jeong.runninggoaltracker.domain.usecase.AddRunningRecordUseCase
import com.jeong.runninggoaltracker.domain.util.DateProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class RunningTrackerService : Service() {

    @Inject
    lateinit var stateUpdater: RunningTrackerStateUpdater

    @Inject
    lateinit var addRunningRecordUseCase: AddRunningRecordUseCase

    @Inject
    lateinit var dateProvider: DateProvider

    @Inject
    lateinit var notificationDispatcher: RunningNotificationDispatcher

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var startTimeMillis: Long = 0L
    private var distanceMeters: Double = 0.0
    private var lastLocation: Location? = null
    private var tracking: Boolean = false
    private var elapsedUpdateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationDispatcher.ensureChannel()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startTracking() {
        if (tracking) return

        tracking = true
        distanceMeters = 0.0
        lastLocation = null
        startTimeMillis = System.currentTimeMillis()
        stateUpdater.markTracking()

        startForeground(
            RunningNotificationDispatcher.NOTIFICATION_ID,
            notificationDispatcher.createNotification(0.0, 0L)
        )
        startLocationUpdates()
        startElapsedUpdater()
    }

    private fun stopTracking() {
        if (!tracking) {
            stopSelf()
            return
        }
        tracking = false
        elapsedUpdateJob?.cancel()
        stopLocationUpdates()
        val elapsed = System.currentTimeMillis() - startTimeMillis
        val distanceKm = distanceMeters / METERS_IN_KM
        stateUpdater.stop()

        serviceScope.launch {
            val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsed).toInt()
            if (distanceKm > 0 && durationMinutes > 0) {
                addRunningRecordUseCase(
                    date = dateProvider.getToday(),
                    distanceKm = distanceKm,
                    durationMinutes = durationMinutes
                )
            }
            stopSelf()
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stateUpdater.markPermissionRequired()
            stopTracking()
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MILLIS
        ).setMinUpdateIntervalMillis(UPDATE_INTERVAL_MILLIS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                updateDistance(location)
                val elapsed = System.currentTimeMillis() - startTimeMillis
                stateUpdater.update(distanceMeters / METERS_IN_KM, elapsed)
                notificationDispatcher.notifyProgress(distanceMeters / METERS_IN_KM, elapsed)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback as LocationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    private fun startElapsedUpdater() {
        elapsedUpdateJob?.cancel()
        elapsedUpdateJob = serviceScope.launch {
            while (tracking) {
                val elapsed = System.currentTimeMillis() - startTimeMillis
                stateUpdater.update(distanceMeters / METERS_IN_KM, elapsed)
                notificationDispatcher.notifyProgress(distanceMeters / METERS_IN_KM, elapsed)
                delay(ELAPSED_UPDATE_INTERVAL_MILLIS)
            }
        }
    }

    private fun updateDistance(newLocation: Location) {
        lastLocation?.let { previous ->
            distanceMeters += previous.distanceTo(newLocation).toDouble()
        }
        lastLocation = newLocation
    }

    override fun onDestroy() {
        stopLocationUpdates()
        elapsedUpdateJob?.cancel()
        stateUpdater.stop()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.jeong.runninggoaltracker.action.START_TRACKING"
        const val ACTION_STOP = "com.jeong.runninggoaltracker.action.STOP_TRACKING"

        private const val METERS_IN_KM = 1000.0
        private const val UPDATE_INTERVAL_MILLIS = 2_000L
        private const val ELAPSED_UPDATE_INTERVAL_MILLIS = 1_000L
        private const val MIN_DISTANCE_METERS = 5f

        fun createStopIntent(context: Context): Intent {
            return Intent(context, RunningTrackerService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }
}
