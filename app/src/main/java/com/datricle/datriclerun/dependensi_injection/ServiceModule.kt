package com.datricle.datriclerun.dependensi_injection

import android.app.PendingIntent
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.datricle.datriclerun.R
import com.datricle.datriclerun.others.Constants
import com.datricle.datriclerun.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext app: Context) =
        LocationServices.getFusedLocationProviderClient(app)


    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(@ApplicationContext app: Context): PendingIntent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getActivity(
                app,
                0,
                Intent(app, MainActivity::class.java).also {
                    it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
                },
                PendingIntent.FLAG_MUTABLE
            )
        } else {
            getActivity(
                app,
                0,
                Intent(app, MainActivity::class.java).also {
                    it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        }

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(
        app,
        Constants.NOTIFICATION_CHANNEL_ID
    )
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("Datricle Run")
        .setContentText("00.00.00")
        .setContentIntent(pendingIntent)
}