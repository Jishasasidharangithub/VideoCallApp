package com.image.videocallapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal

class NotificationServiceExtension : OneSignal.OSRemoteNotificationReceivedHandler  {

    private val CHANNEL_ID = "videocall"
    private var notifId = 8547
    private val isHeadsetPlugged: Boolean = false
    private val defaultRingtone: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    private var name:String ?= ""
    private var channelName:String ?= ""
    private var token:String ?= ""
    private var isVoip:String ?= ""

    companion object {
        var vibrator: Vibrator? = null
        var ringtonePlayer: MediaPlayer? = null
        var startedRinging = false
    }

    override fun remoteNotificationReceived(context: Context?, notificationReceivedEvent: OSNotificationReceivedEvent?) {
        val notification = notificationReceivedEvent?.notification?.toJSONObject()
        notification?.apply {
            with(context!!){
                val customData = notificationReceivedEvent.notification?.additionalData
                if (customData != null){
                    name = customData?.getString("name")
                    println("--------------->name$name")
                    channelName = customData?.getString("channel_name")
                    println("--------------->channelName$channelName")
                    token = customData?.getString("token")
                    println("--------------->token$token")
                    isVoip = customData?.getString("isVoip")
                    println("--------------->isVoip$isVoip")

                }
                val customNotificationLayout =
                    RemoteViews(this.packageName, R.layout.activity_call_notification)
                customNotificationLayout.setTextViewText(R.id.tvTitle, "Incoming Call....")
                customNotificationLayout.setTextViewText(R.id.tvDesc,"Anu")

                val acceptIntent = Intent(this, MainActivity::class.java).apply {
                    action = "from-notification"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val cancelIntent = Intent(this, DismissNotificationReceiver::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val pendingCancelBroadcast =
                    PendingIntent.getBroadcast(this, 1, cancelIntent, PendingIntent.FLAG_IMMUTABLE)

                customNotificationLayout.setOnClickPendingIntent(
                    R.id.btnAnswer,
                    getIntentWithAction(acceptIntent, "AcceptButton")
                )
                customNotificationLayout.setOnClickPendingIntent(
                    R.id.btnDeclinee,
                    pendingCancelBroadcast
                )
                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_profile)
                    //.setContentTitle("title")
                    //.setContentText("description")
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setCustomContentView(customNotificationLayout)
                    .setVibrate(longArrayOf(0))
                    //.setSound(defaultRingtone, AudioManager.STREAM_RING)
                    .setAutoCancel(true)
                    .setTimeoutAfter(30000)
                    .setDeleteIntent(pendingCancelBroadcast)
                    .setContentIntent(getIntentWithAction(acceptIntent, "ContentIntent"))
                    .setFullScreenIntent(
                        getIntentWithAction(acceptIntent, "FullScreenIntent"),
                        true
                    )
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                with(notificationManager) {
                    buildChannel()
                    val customNotification = builder.build()
                    notify(notifId, customNotification)
                }

                if (!startedRinging) {
                    startRingtoneAndVibration()
                    startedRinging = true
                }
                // If complete isn't call within a time period of 25 seconds, OneSignal internal logic will show the original notification
                // To omit displaying a notification, pass `null` to complete()
                notificationReceivedEvent.complete(null)
            }
        }
    }
    private fun Context.getIntentWithAction(intent: Intent, action: String): PendingIntent {
        intent.action = action
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
    private fun NotificationManager.buildChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //customRingtone = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.sample)
            val name = "video call"
            val descriptionText = "This channel is used for video consultation"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(false)
                enableVibration(false)
                setBypassDnd(true)
                setSound(
                    defaultRingtone,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_RING) // if headphones are plugged  AudioManager.STREAM_VOICE_CALL
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )

            }
            createNotificationChannel(channel)
        }
    }
    private fun Context.vibratePhone() {
        try {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator?.vibrate(longArrayOf(500, 500, 500), 0)
        } catch (e: Exception) {
        }
    }
    private fun Context.startRingtoneAndVibration() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val needRing = am.ringerMode != AudioManager.RINGER_MODE_SILENT
        if (needRing) {
            ringtonePlayer = MediaPlayer()
            ringtonePlayer?.setOnPreparedListener(MediaPlayer.OnPreparedListener { mediaPlayer: MediaPlayer? ->
                try {
                    ringtonePlayer?.start()
                } catch (e: Throwable) {
                }
            })
            ringtonePlayer?.isLooping = true
            if (isHeadsetPlugged) {
                ringtonePlayer?.setAudioStreamType(AudioManager.STREAM_VOICE_CALL)
            } else {
                ringtonePlayer?.setAudioStreamType(AudioManager.STREAM_RING)
            }
            try {
                ringtonePlayer?.setDataSource(applicationContext, defaultRingtone)
                ringtonePlayer?.prepareAsync()
            } catch (e: java.lang.Exception) {
                ringtonePlayer?.release()
                ringtonePlayer = null
            }
            vibratePhone()
        }
    }
}
