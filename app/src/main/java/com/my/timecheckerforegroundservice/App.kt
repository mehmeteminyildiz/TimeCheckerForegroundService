package com.my.timecheckerforegroundservice

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
created by Mehmet E. Yıldız
 **/
@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}