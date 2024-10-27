package com.test.loginfirebase

import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.test.loginfirebase.databinding.ActivityVoiceCallBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

class VoiceCall : AppCompatActivity() {

    private val app_Id = "72c670b8804f42328b05cbbb08e5e244" // Fill in your App ID from the Agora console
    private val channelName = "elachat" // Fill in your channel name
    private val token = "007eJxTYMioS9myNG6B4pTrS54y3z7YXCv4/+3nqjd+Hw/Yr2hZWZ2qwGBulGxmbpBkYWFgkmZiZGxkkWRgmpyUlGRgkWqaamRi0vNLNr0hkJGBlcOZhZEBAkF8dobUnMTkjMQSBgYAc+kiFw==" // Fill in the temporary Token generated in the Agora console
    private var mRtcEngine: RtcEngine? = null
    private lateinit var binding: ActivityVoiceCallBinding

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                mRtcEngine?.setEnableSpeakerphone(false)
                Toast.makeText(this@VoiceCall, "Join channel success", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                Toast.makeText(this@VoiceCall, "User joined: $uid", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(this@VoiceCall, "User offline: $uid", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun initializeAndJoinChannel() {
        try {
            // Create an RtcEngineConfig object and configure it
            val config = RtcEngineConfig().apply {
                mContext = applicationContext
                mAppId = app_Id
                mEventHandler = mRtcEventHandler
            }
            mRtcEngine = RtcEngine.create(config)
            mRtcEngine?.setEnableSpeakerphone(false)

            // Additional AudioManager routing setup
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.isSpeakerphoneOn = false
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        } catch (e: Exception) {
            throw RuntimeException("Check the error: ${e.message}")
        }

        // Create a ChannelMediaOptions object and configure it
        val options = ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            publishMicrophoneTrack = true
            autoSubscribeAudio = true
        }

        // Join the channel
        mRtcEngine?.joinChannel(token, channelName, 0, options)
    }

    private val PERMISSION_REQ_ID = 22


    private fun getRequiredPermissions(): Array<String> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.BLUETOOTH
            )
        } else {
            arrayOf(android.Manifest.permission.RECORD_AUDIO)
        }
    }


    private fun checkPermissions(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val receiverName = intent.getStringExtra("name")
        val receiverImage = intent.getStringExtra("picture")
        // Get the extras from the intent


        // If already authorized, initialize the RtcEngine and join the channel
        if (checkPermissions()) {
            initializeAndJoinChannel()
            binding.userName.text = receiverName
            Glide.with(this)
                .load(receiverImage)
                .placeholder(R.drawable.portrait_placeholder)
                .error(R.drawable.portrait_placeholder)
                .into(binding.imageView)

        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID)
        }

        binding.leaveCallButton.setOnClickListener {
            mRtcEngine?.let {
                it.leaveChannel()
                RtcEngine.destroy()
                mRtcEngine = null
                finish()
            }
        }
    }

    // System permission request callback
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermissions()) {
            initializeAndJoinChannel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine?.let {
            it.leaveChannel()
            RtcEngine.destroy()
            mRtcEngine = null
        }
    }
}