package com.test.loginfirebase

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.internal.service.Common
import com.test.loginfirebase.databinding.ActivityVideoCallBinding
import com.test.loginfirebase.utils.CommonUtil
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

class VideoCall : AppCompatActivity() {

    private lateinit var binding: ActivityVideoCallBinding


    private val appId = "72c670b8804f42328b05cbbb08e5e244"

    var appCertificate = "876add27ba0541e49c68c66ce60236b6"
    var expirationTimeInSeconds = 3600
    private val channelName = "elachat"

    // private var token : String? = null
    private var token = "007eJxTYNDLfCusdmONcLyORWvsQZONbXZBnsxfYvhrvvo7fNI7OE2Bwdwo2czcIMnCwsAkzcTI2MgiycA0OSkpycAi1TTVyMRk7af7aQ2BjAyhpc+ZGRkgEMRnZ0jNSUzOSCxhYAAAFkwfig=="

    private val uid = 0
    private var isJoined = false

    private var agoraEngine: RtcEngine? = null

    private var remoteSurfaceView: SurfaceView? = null

    private var localSurfaceView: SurfaceView? = null


    private val PERMISSION_REQ_ID = 22

    private val REQUESTED_PERMISSIONS = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.CAMERA,
    )

    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(
            this,
            REQUESTED_PERMISSIONS[0]
        ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    REQUESTED_PERMISSIONS[1]
                ) != PackageManager.PERMISSION_GRANTED)
    }

    fun showMessage(message: String?) {
        runOnUiThread {
           /* Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_SHORT
            ).show()*/

            CommonUtil.showToastMessage(applicationContext,message!!)
        }
    }


    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageBack.setOnClickListener {
            finish()
        }
        binding.remoteVideoViewContainer.visibility = View.GONE

        binding.leaveCallButton.visibility = View.GONE

        /*val tokenBuilder = RtcTokenBuilder2()
        val timestamp = (System.currentTimeMillis() / 1000 + expirationTimeInSeconds).toInt()

        println("UID token")
        val result = tokenBuilder.buildTokenWithUid(
            appId, appCertificate,
            channelName, uid, RtcTokenBuilder2.Role.ROLE_PUBLISHER, timestamp, timestamp
        )
        println(result)


        token = result*/

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        setupVideoSDKEngine()

        binding.joinCallButton.setOnClickListener {
            joinChannel()
        }
        binding.leaveCallButton.setOnClickListener {
            leaveChannel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()

        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            // Set the remote video view
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")
            runOnUiThread { binding.remoteVideoViewContainer.visibility = View.GONE }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        remoteSurfaceView = SurfaceView(baseContext)
        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        binding.remoteVideoViewContainer.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                uid
            )
        )
        binding.remoteVideoViewContainer.visibility = View.VISIBLE
    }

    private fun setupLocalVideo() {
        localSurfaceView = SurfaceView(baseContext)
        binding.localVideoViewContainer.addView(localSurfaceView)
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
        binding.localVideoViewContainer.visibility = View.VISIBLE
    }

    private fun joinChannel() {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()

            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            setupLocalVideo()
            binding.localVideoViewContainer.visibility = View.VISIBLE
            binding.localVideoViewContainer.animate()
                .alpha(1.0f)
                .setDuration(500)
                .withStartAction { binding.localVideoViewContainer.visibility = View.VISIBLE }
                .start()
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(token, channelName, uid, options)
            binding.leaveCallButton.visibility = View.VISIBLE
        } else {
            /*Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT)
                .show()*/
            CommonUtil.showToastMessage(applicationContext,"Permissions was not granted")
        }
    }

    private fun leaveChannel() {
        if (!isJoined) {
            showMessage("Join a channel first")
        } else {
            agoraEngine!!.leaveChannel()
            showMessage("You left the channel")
            if (remoteSurfaceView != null) binding.remoteVideoViewContainer.visibility = View.GONE
            if (localSurfaceView != null) binding.localVideoViewContainer.visibility = View.GONE
            isJoined = false
            binding.leaveCallButton.visibility = View.GONE
        }
    }
}