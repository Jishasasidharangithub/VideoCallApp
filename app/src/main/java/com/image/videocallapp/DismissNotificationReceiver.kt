package com.image.videocallapp

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.image.videocallapp.NotificationServiceExtension.Companion.ringtonePlayer
import com.image.videocallapp.NotificationServiceExtension.Companion.startedRinging
import com.image.videocallapp.NotificationServiceExtension.Companion.vibrator

class DismissNotificationReceiver : BroadcastReceiver()  {
    override fun onReceive(context: Context?, intent: Intent?) {
        stopRinging()
        val notificationId = 8547 //intent.getIntExtra("notification_id", 0)
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
    private fun stopRinging() {
        startedRinging = false
        if (ringtonePlayer != null) {
            ringtonePlayer?.stop()
            ringtonePlayer?.release()
            ringtonePlayer = null
        }
        if (vibrator != null) {
            vibrator?.cancel()
            vibrator = null
        }
    }
}
