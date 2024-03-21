package com.aliashraf.assignment2
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import java.lang.Exception

class AudioCallActivity : AppCompatActivity() {

    private val APP_ID = "3587de851a784cca841a1d4df12bd590"
    private val CHANNEL = "Test_Test"
    private val TOKEN = "007eJxTYMi7v77h7/HMHRtP7dh0vC/Ln81//n896y8Juj2hMzX3HuJQYDA2tTBPSbUwNUw0tzBJTk60MDFMNEwxSUkzNEpKMbU0uFT5I7UhkJFhpwUzEyMDBIL4nAwhqcUl8SCCgQEAsXMjSw=="
    private var mRtcEngine: RtcEngine? = null
    private var localUid: Int = 0
    private var isMuted = false
    private var isDeafened = false
    private lateinit var muteButton: ImageView
    private lateinit var deafenButton: ImageView

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d("YES", "Remote user joined: $uid")
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.d("YES", "Joined channel: $channel, uid: $uid")
            localUid = uid
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d("YES", "Remote user offline: $uid, reason: $reason")
        }

        override fun onError(err: Int) {
            Log.e("YES", "Error code: $err")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_call)

        val endCallBtn: View = findViewById(R.id.endCallBtn)
        muteButton = findViewById(R.id.microphoneIcon)
        deafenButton = findViewById(R.id.speaker)

        endCallBtn.setOnClickListener {
            finish()
        }

        muteButton.setOnClickListener {
            toggleMute()
        }

        deafenButton.setOnClickListener {
            toggleDeafen()
        }

        initializeAndJoinChannel()
    }

    private fun initializeAndJoinChannel() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)
        } catch (e: Exception) {
            Log.e("YES", "Failed to create RtcEngine: ${e.message}")
        }

        val options = ChannelMediaOptions()
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

        mRtcEngine?.joinChannel(TOKEN, CHANNEL, localUid, options)
        Log.d("YES", "Call started")
    }

    private fun toggleMute() {
        if (isMuted) {
            mRtcEngine?.adjustRecordingSignalVolume(100)
            muteButton.setImageResource(R.drawable.ic_mic_on) // Change to your unmuted icon
            isMuted = false
        } else {
            mRtcEngine?.adjustRecordingSignalVolume(0)
            muteButton.setImageResource(R.drawable.ic_mic_off) // Change to your muted icon
            isMuted = true
        }
    }

    private fun toggleDeafen() {
        if (isDeafened) {
            mRtcEngine?.adjustPlaybackSignalVolume(100)
            deafenButton.setImageResource(R.drawable.ic_speaker) // Change to your undeafened icon
            isDeafened = false
        } else {
            mRtcEngine?.adjustPlaybackSignalVolume(0)
            deafenButton.setImageResource(R.drawable.ic_volume_off) // Change to your deafened icon
            isDeafened = true
        }
    }

    override fun onStop() {
        super.onStop()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        Log.d("YES", "Call ended")
    }
}
