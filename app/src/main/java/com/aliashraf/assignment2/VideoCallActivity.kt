package com.aliashraf.assignment2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceView
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.Constants
import io.agora.rtc2.video.VideoEncoderConfiguration


class VideoCallActivity : AppCompatActivity() {
    private var isVideoEnabled = true // Flag to track if video is enabled or not
    private var isMuted = false
    private val APP_ID = "3587de851a784cca841a1d4df12bd590"
    private lateinit var muteButton: ImageView
    private lateinit var switchCameraBtn: ImageView
    private lateinit var cutVideoBtn: ImageView
    private lateinit var localContainer: FrameLayout

    private val CHANNEL = "Test_Test"
    private val TOKEN = "007eJxTYOBxMFh+Ye3W75F7ohZJ/W5kOiijtzCoVFaGNbBYJC1p51QFBmNTC/OUVAtTw0RzC5Pk5EQLE8NEwxSTlDRDo6QUU0uDtad/pjYEMjIoatmzMjJAIIjPyRCSWlwSDyIYGABSkR9L"
    private val PERMISSION_REQ_ID_RECORD_AUDIO = 22
    private val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    private var mRtcEngine: RtcEngine ?= null
    var localid: Int = 0
    var uids: Int = 0
    var remoteUids = HashSet<Int>()
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote user joining the channel to get the uid of the user.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d("YES", "Remote user joined: $uid")
            runOnUiThread {
                uids = uid
                // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
                setupRemoteVideo(uid)
            }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.d("YES", "Joined channel: $channel, uid: $uid")
            // Handle the event when the local user successfully joins the channel
            localid = uid
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d("YES", "Remote user offline: $uid, reason: $reason")
            remoteUids.remove(uid)
        }
        override fun onError(err: Int) {
            when (err) {
                ErrorCode.ERR_TOKEN_EXPIRED -> Log.e("YES", "Your token has expired")
                ErrorCode.ERR_INVALID_TOKEN -> Log.e("YES", "Your token is invalid")
                else -> Log.e("YES", "Error code: $err")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        Log.d("YES", "onCreate")

        switchCameraBtn = findViewById(R.id.pause)
        muteButton = findViewById(R.id.microphoneIcon)
        cutVideoBtn = findViewById(R.id.speaker)
        //mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            initializeAndJoinChannel()
        }
        delayExecution(2000) { toggleVideoCut() }
        delayExecution(2000) { toggleVideoCut() }

        switchCameraBtn.setOnClickListener {
            switchCamera()
        }

        muteButton.setOnClickListener {
            toggleMute()
        }

        cutVideoBtn.setOnClickListener {
            toggleVideoCut()
        }

        val endCallBtn: View = findViewById(R.id.endCallBtn)
        endCallBtn.setOnClickListener {
            finish()
        }
    }

    private fun switchCamera() {
        mRtcEngine?.switchCamera()
        Log.d("YES", "switchCamera: Camera switched")
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

    private fun toggleVideoCut() {
        if (isVideoEnabled) {
            mRtcEngine?.disableVideo()
            Log.d("YES", "toggleVideoCut: Video cut")
            localContainer?.removeAllViews() // Remove any existing views from the container
            val imageView = ImageView(this)
            imageView.setImageResource(R.drawable.btn_style) // Set black image resource
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            localContainer?.addView(imageView, layoutParams) // Add ImageView to the container
            cutVideoBtn.setImageResource(R.drawable.ic_video_off) // Change to your unmuted icon
        } else {
            initializeAndJoinChannel()
            Log.d("YES", "toggleVideoCut: Video restored")
            cutVideoBtn.setImageResource(R.drawable.ic_video_on) // Change to your muted icon
        }
        isVideoEnabled = !isVideoEnabled
    }
    private fun initializeAndJoinChannel() {
        Log.d("YES", "initializeAndJoinChannel: Initializing RTC Engine")
        try {
            mRtcEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)
            mRtcEngine!!.enableVideo()
            mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            Log.d("YES", "initializeAndJoinChannel: RTC Engine initialized successfully")
        } catch (e: Exception) {
            Log.e("YES", "initializeAndJoinChannel: Failed to initialize RTC Engine: ${e.message}")
            // Handle the exception if needed
        }

        // Check if mRtcEngine is not null
        if (mRtcEngine != null) {
            Log.d("YES", "initializeAndJoinChannel: Enabling video module")
            // Enable video module
            mRtcEngine!!.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.COMPATIBLE_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                )
            )
            // Find the local video container
            localContainer = findViewById<FrameLayout>(R.id.local_video_view_container)
            // Check if localContainer is not null
            if (localContainer != null) {
                Log.d("YES", "initializeAndJoinChannel: Local video container found")
                // Create a SurfaceView for the local video
                val localFrame = RtcEngine.CreateRendererView(baseContext)
                // Add the local video view to the container
                localFrame.setZOrderMediaOverlay(true)
                localContainer!!.addView(localFrame)
                Log.d("YES", "initializeAndJoinChannel: Local video view added to container")
                // Setup local video with the created SurfaceView
                mRtcEngine?.setupLocalVideo(VideoCanvas(localFrame, VideoCanvas.RENDER_MODE_ADAPTIVE, 0))
                Log.d("YES", "initializeAndJoinChannel: Local video setup completed")
            } else {
                // Handle the case when localContainer is null
                Log.e("YES", "initializeAndJoinChannel: Local video container not found")
            }

            // Delay enabling local video after adding it to the view
//            delayExecution(2000) {
                // Join the channel with a token
                Log.d("YES", "initializeAndJoinChannel: Joining channel")
                mRtcEngine?.joinChannel(TOKEN, CHANNEL, "", 0)
                Log.d("YES", "initializeAndJoinChannel: Channel joined")
 //           }
        } else {
            Log.e("YES", "initializeAndJoinChannel: RTC Engine is null")
        }
    }







    private fun setupRemoteVideo(uid: Int) {
        Log.d("YES", "setupRemoteVideo")
        val remoteContainer = findViewById<FrameLayout>(R.id.remote_video_view_container)

        val remoteFrame = RtcEngine.CreateRendererView(baseContext)
        remoteFrame.setZOrderMediaOverlay(true)
        remoteContainer.addView(remoteFrame)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(remoteFrame, VideoCanvas.RENDER_MODE_ADAPTIVE, uid))
    }
    private fun delayExecution(delayMillis: Long, action: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            action.invoke()
        }, delayMillis)
    }

    override fun onDestroy() {
        super.onDestroy()

        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }
    private fun setupLocalVideo(localFrame: SurfaceView?) {
        localFrame?.let { surfaceView ->
            surfaceView.visibility = View.VISIBLE
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            surfaceView.layoutParams = layoutParams
        }
    }
}