package com.image.app

import android.app.Application
import com.image.videocallapp.R
import com.onesignal.OneSignal

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        // Initialize OneSignal
        OneSignal.initWithContext(this);
        OneSignal.setAppId(getString(R.string.one_signal_app_id));
    }
}