package com.datricle.datriclerun.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.datricle.datriclerun.R
import com.datricle.datriclerun.others.Constants.ACTION_PAUSE_SERVICE
import com.datricle.datriclerun.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.datricle.datriclerun.others.Constants.ACTION_STOP_SERVICE
import com.datricle.datriclerun.others.Constants.FASTEST_LOCATION_INTERVAL
import com.datricle.datriclerun.others.Constants.LOCATION_UPDATE_INTERVAL
import com.datricle.datriclerun.others.Constants.NOTIFICATION_CHANNEL_ID
import com.datricle.datriclerun.others.Constants.NOTIFICATION_CHANNEL_NAME
import com.datricle.datriclerun.others.Constants.NOTIFICATION_Id
import com.datricle.datriclerun.others.Constants.TIMER_UPDATE_INTERVAL
import com.datricle.datriclerun.others.TrackingUtilities
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias  Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKill = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder


    companion object {
        val timeRunInMillie = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()

    }

    private fun postInitializeValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillie.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitializeValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun killService() {
        serviceKill = true
        isFirstRun = true
        pauseService()
        postInitializeValues()
        stopForeground(true)
        stopSelf()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming Service")
                        startTimer()
                    }
                    Timber.d("Started or resume service")
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("PausedService")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop Service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted

                timeRunInMillie.postValue(timeRun + lapTime)

                if (timeRunInMillie.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime

        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_MUTABLE)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_MUTABLE)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKill) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_Id, curNotificationBuilder.build())
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtilities.hasLocationPermissions(this)) {
                Log.d("no permissions", "${TrackingUtilities.hasLocationPermissions(this)}")
                val request = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY

                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("New Location: ${location.latitude},${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_Id, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            if (!serviceKill) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtilities.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_Id, notification.build())
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
        notificationManager.createNotificationChannel(channel)
    }
}