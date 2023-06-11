package com.my.timecheckerforegroundservice.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.my.timecheckerforegroundservice.R
import com.my.timecheckerforegroundservice.model.TimerEvent
import com.my.timecheckerforegroundservice.util.Constants
import com.my.timecheckerforegroundservice.util.TimerUtil
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

/**
created by Mehmet E. Yıldız
 **/
@AndroidEntryPoint
class TimeCheckerService : LifecycleService() {
    private var timer: Timer? = null
    private var viewShowing = false
    private var isLogin = false

    val handler = Handler(Looper.getMainLooper())
    private var isServiceStopped = false

    companion object {
        val timerEvent = MutableLiveData<TimerEvent>()
        val timerInMillis = MutableLiveData<Long>()
    }

    private var timeList = arrayListOf<String>("17:48:00", "17:48:15", "17:48:30","17:48:45")



    // overlay
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var params: WindowManager.LayoutParams? = null

    private var myContext: Context? = null

    @Inject
    lateinit var notificationManager: NotificationManagerCompat

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        Timber.tag("XYZ").e("onCreate")
        myContext = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag("XYZ").e("onStartCommand")
        intent?.let {
            when (it.action) {
                Constants.ACTION_START_SERVICE -> startForegroundService()
                Constants.ACTION_STOP_SERVICE -> stopForegroundService()
            }
        }
        startTimer()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        timerEvent.postValue(TimerEvent.START)
        startTimer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        startForeground(Constants.NOTIFICATION_ID, notificationBuilder.build())
        observeMillis()
    }


    /** Notification'da bulunan timer'ı sürekli günceller **/
    private fun observeMillis() {
        timerInMillis.observe(
            this,
            Observer {
                if (!isServiceStopped) {
                    val builder = notificationBuilder.setContentText(
                        TimerUtil.getFormattedTime(it, false)
                    )
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@Observer
                    }
                    notificationManager.notify(Constants.NOTIFICATION_ID, builder.build())
                }
            },
        )
    }

    private fun initValues() {
        timerEvent.postValue(TimerEvent.END)
        timerInMillis.postValue(0L)
    }

    private fun stopForegroundService() {
        isServiceStopped = true
        initValues()
        notificationManager.cancel(Constants.NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
    }

    private fun startTimer() {
        Timber.tag("XYZ").e("startTimer")

        timer = Timer()
        val timerTask = timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val time = System.currentTimeMillis()
                val formattedTime = TimerUtil.getTime(time)
                Timber.tag("XYZ").e("formattedTime : $formattedTime")

                if (timeList.contains(formattedTime)) {
                    Timber.tag("XYZ").e("Zaman Geldi")
                    handler.post {
                        Timber.tag("XYZ").e("handler.post")
                        overlayView =
                            LayoutInflater.from(myContext).inflate(R.layout.overlay_layout, null)
                        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                        params = WindowManager.LayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                            PixelFormat.TRANSLUCENT
                        )
                        params?.gravity = Gravity.CENTER

                        windowManager?.addView(overlayView, params)

                        overlayView?.let { overlayView ->
                            overlayView.setOnKeyListener { v, keyCode, event ->
                                Timber.e("keyCode : $keyCode")
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    // Geri tuşuna basıldığında yapılacak işlemler
                                    Timber.e("back basıldı")
                                    // ...
                                    true
                                } else {
                                    Timber.e("else basıldı")
                                    false
                                }
                            }

                            overlayView.setOnLongClickListener(object : View.OnLongClickListener {
                                override fun onLongClick(v: View?): Boolean {
                                    viewShowing = false
                                    isLogin = true
                                    windowManager?.removeView(overlayView)
                                    return true
                                }

                            })
                            // remove için:

//
                        }
                    }
                }
            }
        }, 0, 1000 * 1) // Her 1 saniyede bir çalışacak şekilde ayarlanmıştır

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        startForeground(Constants.NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    /** Notification channel oluşturulur **/
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }


}