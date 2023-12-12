package com.image.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.image.videocallapp.NotificationForegroundHandler
import com.image.videocallapp.NotificationOpenedHandler
import com.image.videocallapp.R
import com.image.videocallapp.SessionSaveUser
import com.onesignal.BuildConfig
import com.onesignal.OSSubscriptionObserver
import com.onesignal.OSSubscriptionStateChanges
import com.onesignal.OneSignal

class MyApplication : Application(), OSSubscriptionObserver {

    private var sessionSave: SessionSaveUser? = null

    override fun onCreate() {
        super.onCreate()

        //createNotificationChannel()
        //removeBrokenNotificationChannel()
        configureOneSignal()

    }
    override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges?) {
        val playerId = stateChanges?.to?.userId
        println("OneSignal_playerId==  $playerId")
        sessionSave = SessionSaveUser(this@MyApplication).getInstance()
        sessionSave?.putSharedString("deviceToken", playerId)

    }
    private fun configureOneSignal() {
        // OneSignal Initialization
        if(BuildConfig.DEBUG) {
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        }else{
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.NONE, OneSignal.LOG_LEVEL.NONE)
        }
        OneSignal.initWithContext(this)
        OneSignal.setAppId(getString(R.string.one_signal_app_id))

        OneSignal.addSubscriptionObserver(this)
        /*OneSignal.setNotificationOpenedHandler(NotificationOpenedHandler(this))
        OneSignal.setNotificationWillShowInForegroundHandler(
            NotificationForegroundHandler(this)
        )*/
    }
    /*private fun createNotificationChannel() {
        // Create a NotificationManager
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()

        // Check if the device is running Android Oreo or higher, and create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "7a9a556f-ed68-4b91-8e95-77be6963bda0",
                "IncomingCall",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lightColor = Color.BLUE
                enableLights(true)
                enableVibration(true)
                setSound(ringtone, audioAttributes)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
                description = ""
            }
            notificationManager.createNotificationChannel(channel)
        }
    }*/
   /* private fun removeBrokenNotificationChannel() {
        removeNotificationChannel("7a9a556f-ed68-4b91-8e95-77be6963bda0")
    }
    private fun removeNotificationChannel(channelId: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(channelId)
        }
    }*/
}