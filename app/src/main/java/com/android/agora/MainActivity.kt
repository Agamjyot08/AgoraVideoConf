package com.android.agora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff
import android.util.Log
import android.view.SurfaceView;
import android.view.View
import android.widget.FrameLayout;
import android.widget.ImageView

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

class MainActivity : AppCompatActivity() {

    // Kotlin
    // Fill the App ID of your project generated on Agora Console.
    private val APP_ID = "f6181a4c31b14c80a83607d8118c7b9e"
    // Fill the channel name.
    private val CHANNEL = "PhysioAI"
    // Fill the temp token generated on Agora Console.
    private val TOKEN = "006f6181a4c31b14c80a83607d8118c7b9eIAANXV4SdJQVRnC9jXVYWpSCgH5Auq6GDe0GpixAsV3BQt9bxfkAAAAAEABhfJB/Oh+zYQEAAQA5H7Nh"

    private var mRtcEngine: RtcEngine ?= null

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote user joining the channel to get the uid of the user.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
                setupRemoteVideo(uid)
            }
        }
    }

    // Kotlin
    private val PERMISSION_REQ_ID_RECORD_AUDIO = 22
    private val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(permission),
                requestCode)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initializeAndJoinChannel()
        }

    }

    // Kotlin
    override fun onDestroy() {
        super.onDestroy()

        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }

    // Kotlin
    private fun initializeAndJoinChannel() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)
        } catch (e: Exception) {

        }

        // By default, video is disabled, and you need to call enableVideo to start a video stream.
        mRtcEngine!!.enableVideo()

        val localContainer = findViewById(R.id.local_video_view_container) as FrameLayout
        // Call CreateRendererView to create a SurfaceView object and add it as a child to the FrameLayout.
        val localFrame = RtcEngine.CreateRendererView(baseContext)
        localContainer.addView(localFrame)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        mRtcEngine!!.setupLocalVideo(VideoCanvas(localFrame, VideoCanvas.RENDER_MODE_FIT, 0))

        // Join the channel with a token.
        mRtcEngine!!.joinChannel(TOKEN, CHANNEL, "", 0)
    }

    // Kotlin
    private fun setupRemoteVideo(uid: Int) {
        val remoteContainer = findViewById(R.id.remote_video_view_container) as FrameLayout

        val remoteFrame = RtcEngine.CreateRendererView(baseContext)
        remoteFrame.setZOrderMediaOverlay(true)
        remoteContainer.addView(remoteFrame)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(remoteFrame, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    fun onEncCallClicked(view: android.view.View) {
        finish()
    }

    fun onSwitchCameraClicked(view: android.view.View) {
        mRtcEngine!!.switchCamera()
    }

    fun onLocalAudioMuteClicked(view: android.view.View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.MULTIPLY)
        }

        // Stops/Resumes sending the local audio stream.
        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }

    fun onLocalVideoMuteClicked(view: android.view.View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.MULTIPLY)
        }
        // Stops/Resumes sending the local video stream.
        mRtcEngine!!.muteLocalVideoStream(iv.isSelected)

        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
        val surfaceView = container.getChildAt(0) as SurfaceView
        surfaceView.setZOrderMediaOverlay(!iv.isSelected)
        surfaceView.visibility = if (iv.isSelected) View.GONE else View.VISIBLE
    }

    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
        } catch (e: Exception) {


            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }

}