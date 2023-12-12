package com.image.videocallapp

import android.content.Context
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal

class NotificationOpenedHandler (private val context: Context) : OneSignal.OSNotificationOpenedHandler {
    override fun notificationOpened(result: OSNotificationOpenedResult) {
        // Handle the notification click here
        val customData = result.notification?.additionalData
        if (customData != null) {
            val name = customData.optString("name")
            val channelName = customData.optString("channel_name")
            val token = customData.optString("token")

            // Use the custom data as needed
            println("Custom Data - Name: $name, Channel Name: $channelName, Token: $token")

        }
    }
}