package com.image.videocallapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class CustomNotificationManager(private val context: Context) {

    fun showIncomingCallNotification() {
        val notificationLayout = RemoteViews(context.packageName, R.layout.activity_call_notification)

        // Customize notification layout components and actions here

        val notification = NotificationCompat.Builder(context, "7a9a556f-ed68-4b91-8e95-77be6963bda0")
            .setSmallIcon(R.drawable.ic_profile)
            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                 context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(1, notification)
    }
}