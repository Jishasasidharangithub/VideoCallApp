package com.image.videocallapp

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.image.videocallapp.databinding.ActivityMainBinding
import com.onesignal.OneSignal
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas


class MainActivity : AppCompatActivity() {

    private var binding:ActivityMainBinding ?= null

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.CAMERA
    )

    private fun checkSelfPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED)
        {
            return false
        }
        return true
    }

    private var appId = "07ea2876544942f6bf279730fd008e10"
    private var channelName = "demovideocall"
    private var token = "007eJxTYDg1v5GV4+GRrgf7b0nsr1q9eo3bpeMxc7hXPS4+F26f4HRAgcHAPDXRyMLczNTExNLEKM0sKc3I3NLc2CAtxcDAItXQYN/dotSGQEaGOoPVTIwMEAji8zKkpObml2WmpOYnJ+bkMDAAACSdJL0="
    private var uid = 0
    private var isJoined:Boolean = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null
    private lateinit var customNotificationManager: CustomNotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        customNotificationManager = CustomNotificationManager(this)

        // Initialize OneSignal and create notification channel
        OneSignal.initWithContext(this)
        OneSignal.setAppId("ffba91b9-9130-415b-a6c1-281ce88c5d73")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "7a9a556f-ed68-4b91-8e95-77be6963bda0"
            val channelName = "IncomingCall....."
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(channelId, channelName, importance)
            val notificationManager =
                getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        OneSignal.setNotificationOpenedHandler {
            // Handle OneSignal notification opened event here
            customNotificationManager.showIncomingCallNotification()

            // Add logic to open your video call activity or join the call
            // For example, you can call the joinChannel method
            joinChannel()
        }
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }
        setupVideoSDKEngine()

    }

    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine?.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }

    val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            // Set the remote video view
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")
            runOnUiThread { remoteSurfaceView?.visibility = View.GONE }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val container: FrameLayout? = binding?.remoteVideoViewContainer
        remoteSurfaceView = SurfaceView(baseContext)
        remoteSurfaceView?.setZOrderMediaOverlay(true)
        container?.addView(remoteSurfaceView)
        agoraEngine?.setupRemoteVideo(VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
        // Display RemoteSurfaceView.
        remoteSurfaceView?.visibility = View.VISIBLE
    }
    private fun setupLocalVideo() {
        val container: FrameLayout? = binding?.localVideoViewContainer
        // Create a SurfaceView object and add it as a child to the FrameLayout.
        localSurfaceView = SurfaceView(baseContext)
        container?.addView(localSurfaceView)
        // Call setupLocalVideo with a VideoCanvas having uid set to 0.
        agoraEngine?.setupLocalVideo(VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    fun joinChannel() {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            // For a Video call, set the channel profile as COMMUNICATION.
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            // Display LocalSurfaceView.
            setupLocalVideo()
            localSurfaceView?.visibility = View.VISIBLE
            // Start local preview.
            agoraEngine?.startPreview()
            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            agoraEngine?.joinChannel(token, channelName, uid, options)
        } else {
            Toast.makeText(applicationContext, "Permissions were not granted", Toast.LENGTH_SHORT).show()
        }
    }
    fun leaveChannel(view: View) {
        if (!isJoined) {
            showMessage("Join a channel first")
        } else {
            agoraEngine?.leaveChannel()
            showMessage("You left the channel")
            // Stop remote video rendering.
            remoteSurfaceView?.visibility = View.GONE
            // Stop local video rendering.
            localSurfaceView?.visibility = View.GONE
            isJoined = false
        }
    }

    fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine?.stopPreview()
        agoraEngine?.leaveChannel()

        // Destroy the engine in a sub-thread to avoid congestion
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

}