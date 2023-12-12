package com.image.videocallapp

import android.content.Context
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal

class NotificationForegroundHandler (private val context: Context) : OneSignal.OSNotificationWillShowInForegroundHandler {

    override fun notificationWillShowInForeground(notificationReceivedEvent: OSNotificationReceivedEvent) {
        // Handle the notification when the app is in the foreground
        val customData = notificationReceivedEvent.notification?.additionalData
        if (customData != null) {
            val name1 = customData.optString("name")
            val channelName1 = customData.optString("channel_name")
            val token1 = customData.optString("token")

            // Use the custom data as needed
            //println("Custom Data - Name: $name1, Channel Name: $channelName1, Token: $token1")
        }
    }
}