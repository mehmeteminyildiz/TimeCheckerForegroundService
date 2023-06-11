package com.my.timecheckerforegroundservice.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.my.timecheckerforegroundservice.MainActivity
import com.my.timecheckerforegroundservice.R
import com.my.timecheckerforegroundservice.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import timber.log.Timber

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {


    /** NotificationManagerCompat'ın sağlanması **/
    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ) = NotificationManagerCompat.from(context)

    /** NotificationManagerCompat.Builder'ın sağlanması **/
    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat
        .Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.baseline_timer_24)
        .setContentTitle("Timer")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)

    /** PendingIntent'in sağlanması **/
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext context: Context,
    ): PendingIntent {
        Timber.e("provideMainActivityPendingIntent")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val flags = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            }

            else -> PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getActivity(
            context,
            143,
            intent,
            flags
        )
    }


}