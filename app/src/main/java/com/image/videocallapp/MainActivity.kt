package com.image.videocallapp

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import org.json.JSONObject


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
        { return false
        }
        return true
    }
    private var appId = "07ea2876544942f6bf279730fd008e10"
    private var channelName = "DemoVideoCall"
    private var token = "007eJxTYFjN+f/K6pdBFdEX9863TVrTE+A9OWxqRE8IV5+H0J5NLb4KDAbmqYlGFuZmpiYmliZGaWZJaUbmlubGBmkpBgYWqYYGegerUhsCGRnmTvzDyMgAgSA+L4NLam5+WGZKar5zYk4OAwMAh+4itQ=="
    private var uid = 0
    private var isJoined:Boolean = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: View? = null
    private var remoteSurfaceView: SurfaceView? = null
    private var isMuted: Boolean = false
    private var isVideoEnabled: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }
        setupVideoSDKEngine()
        setVoiceHandleEvent()
        setVideoHandleEvents()
        if (intent.action == "AcceptButton") {
            joinChannel()
            NotificationServiceExtension.stopRingtone()
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(8547)
            stopVibration()
        }
    }
    private fun setVoiceHandleEvent(){
        binding?.ivVioceOn?.visibility = if (isMuted) View.GONE else View.VISIBLE
        binding?.ivVioceOff?.visibility = if (isMuted) View.VISIBLE else View.GONE
        binding?.ivVioceOn?.setOnClickListener {
            toggleMicrophone(true)
        }
        binding?.ivVioceOff?.setOnClickListener {
            toggleMicrophone(false)
        }
    }
    private fun toggleMicrophone(isMute: Boolean) {
        this.isMuted = isMute
        binding?.ivVioceOn?.visibility = if (isMute) View.GONE else View.VISIBLE
        binding?.ivVioceOff?.visibility = if (isMute) View.VISIBLE else View.GONE
        // Mute or unmute the microphone based on the status
        agoraEngine?.muteLocalAudioStream(isMute)
    }
    private fun setVideoHandleEvents(){
        binding?.ivVideoOn?.visibility = if (isVideoEnabled) View.GONE else View.VISIBLE
        binding?.ivVideoOff?.visibility = if (isVideoEnabled) View.VISIBLE else View.GONE
        binding?.ivVideoOn?.setOnClickListener {
            toggleCamera(true)
        }
        binding?.ivVideoOff?.setOnClickListener {
            toggleCamera(false)
        }
    }
    private fun toggleCamera(isEnable: Boolean) {
        this.isVideoEnabled = isEnable
        binding?.ivVideoOn?.visibility = if (isEnable) View.GONE else View.VISIBLE
        binding?.ivVideoOff?.visibility = if (isEnable) View.VISIBLE else View.GONE
        // Show or hide the local video view based on the status
        localSurfaceView?.visibility = if (isEnable) View.VISIBLE else View.GONE
        agoraEngine?.muteLocalVideoStream(!isEnable)
        // Stop or start the local video preview based on the status
        if (isEnable) {
            agoraEngine?.startPreview()
        } else {
            agoraEngine?.stopPreview()
        }
    }
    private fun stopVibration() {
        if (NotificationServiceExtension.vibrator != null) {
            NotificationServiceExtension.vibrator?.cancel()
        }
    }
    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            agoraEngine?.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }
    val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")
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
        remoteSurfaceView = SurfaceView(baseContext)
        //remoteSurfaceView?.setZOrderMediaOverlay(true)
        binding?.remoteVideoViewContainer?.addView(remoteSurfaceView)
        agoraEngine?.setupRemoteVideo(VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid))
        remoteSurfaceView?.visibility = View.VISIBLE
    }
    private fun setupLocalVideo() {
        localSurfaceView = SurfaceView(baseContext)
        (localSurfaceView as SurfaceView)?.setZOrderMediaOverlay(true)
        binding?.localVideoViewContainer?.addView(localSurfaceView)
        agoraEngine?.setupLocalVideo(VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, 0))
    }
    fun joinChannel() {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            setupLocalVideo()
            /*localSurfaceView?.visibility = View.VISIBLE
            localSurfaceView?.bringToFront()*/
            agoraEngine?.startPreview()
            agoraEngine?.joinChannel(token, channelName, uid, options)

        } else {
            Toast.makeText(applicationContext, "Permissions were not granted", Toast.LENGTH_SHORT).show()
        }
    }
    fun leaveChannel(view: View) {
        finish()
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
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }
}