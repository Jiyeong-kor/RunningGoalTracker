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
import com.jeong.runninggoaltracker.feature.record.contract.RunningTrackerServiceContract
import com.jeong.runninggoaltracker.shared.designsystem.config.NumericResourceProvider
import com.jeong.runninggoaltracker.shared.designsystem.notification.NotificationPermissionGate
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
    private var startTimeMillis: Long? = null
    private var distanceMeters: Double? = null
    private var lastLocation: Location? = null
    private var tracking: Boolean = false
    private var elapsedUpdateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationDispatcher.ensureChannel()
    }

    @RequiresPermission(
        anyOf = [Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            RunningTrackerServiceContract.ACTION_START -> startTracking()
            RunningTrackerServiceContract.ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    @RequiresPermission(
        anyOf = [Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    private fun startTracking() {
        if (tracking) return
        if (!NotificationPermissionGate.canPostNotifications(this)) {
            stateUpdater.markPermissionRequired()
            stopSelf()
            return
        }

        tracking = true
        distanceMeters = NumericResourceProvider.zeroDouble(this)
        lastLocation = null
        startTimeMillis = System.currentTimeMillis()
        stateUpdater.markTracking()

        startForeground(
            NumericResourceProvider.recordNotificationId(this),
            notificationDispatcher.createNotification(
                NumericResourceProvider.zeroDouble(this),
                NumericResourceProvider.zeroLong(this)
            )
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
        val startMillis = startTimeMillis ?: NumericResourceProvider.zeroLong(this)
        val elapsed = System.currentTimeMillis() - startMillis
        val distanceKm =
            (distanceMeters ?: NumericResourceProvider.zeroDouble(this)) / metersInKm()
        stateUpdater.stop()

        serviceScope.launch {
            val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsed).toInt()
            val zeroInt = NumericResourceProvider.zeroInt(this@RunningTrackerService)
            val zeroDouble = NumericResourceProvider.zeroDouble(this@RunningTrackerService)
            if (distanceKm > zeroDouble && durationMinutes > zeroInt) {
                addRunningRecordUseCase(
                    date = dateProvider.getToday(),
                    distanceKm = distanceKm,
                    durationMinutes = durationMinutes
                )
            }
            stopSelf()
        }
    }

    @RequiresPermission(
        anyOf = [Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            stateUpdater.markPermissionRequired()
            stopTracking()
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            updateIntervalMillis()
        ).setMinUpdateIntervalMillis(updateIntervalMillis())
            .setMinUpdateDistanceMeters(minDistanceMeters())
            .build()

        val metersInKmValue = metersInKm()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                updateDistance(location)
                val startMillis =
                    startTimeMillis ?: NumericResourceProvider.zeroLong(
                        this@RunningTrackerService
                    )
                val elapsed = System.currentTimeMillis() - startMillis
                val currentDistance =
                    distanceMeters ?: NumericResourceProvider.zeroDouble(
                        this@RunningTrackerService
                    )
                stateUpdater.update(currentDistance / metersInKmValue, elapsed)
                notificationDispatcher
                    .notifyProgress(currentDistance / metersInKmValue, elapsed)
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

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    private fun startElapsedUpdater() {
        elapsedUpdateJob?.cancel()
        elapsedUpdateJob = serviceScope.launch {
            val metersInKmValue = metersInKm()
            while (tracking) {
                val startMillis =
                    startTimeMillis ?: NumericResourceProvider
                        .zeroLong(this@RunningTrackerService)
                val elapsed = System.currentTimeMillis() - startMillis
                val currentDistance =
                    distanceMeters ?: NumericResourceProvider
                        .zeroDouble(this@RunningTrackerService)
                stateUpdater.update(currentDistance / metersInKmValue, elapsed)
                notificationDispatcher
                    .notifyProgress(currentDistance / metersInKmValue, elapsed)
                delay(elapsedUpdateIntervalMillis())
            }
        }
    }

    private fun updateDistance(newLocation: Location) {
        lastLocation?.let { previous ->
            val currentDistance = distanceMeters ?: NumericResourceProvider.zeroDouble(this)
            distanceMeters = currentDistance + previous.distanceTo(newLocation).toDouble()
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
        fun createStopIntent(context: Context): Intent =
            Intent(context, RunningTrackerService::class.java).apply {
                action = RunningTrackerServiceContract.ACTION_STOP
            }
    }

    private fun metersInKm(): Double = NumericResourceProvider.metersInKm(this)

    private fun updateIntervalMillis(): Long = NumericResourceProvider
        .updateIntervalMillis(this)

    private fun elapsedUpdateIntervalMillis(): Long =
        NumericResourceProvider.elapsedUpdateIntervalMillis(this)

    private fun minDistanceMeters(): Float = NumericResourceProvider
        .minDistanceMeters(this)
}
