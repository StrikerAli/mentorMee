package com.aliashraf.assignment2

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import java.io.*
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class ChatActivity : AppCompatActivity() {
    //private lateinit var agoraEngine: RtcEngine
    private lateinit var whatsAppHandler: WhatsappHandler
    // Kotlin
// Fill the App ID of your project generated on Agora Console.
    private val APP_ID = "3587de851a784cca841a1d4df12bd590"
    // Fill the channel name.
    private val CHANNEL = "Test_Test"
    // Fill the temp token generated on Agora Console.
    private val TOKEN = "007eJxTYBA8UWGZpirXd+tTgL/HoVVv38c+edAeZa7mKvSgac+kL8wKDMamFuYpqRamhonmFibJyYkWJoaJhikmKWmGRkkpppYGMaU/UhsCGRmuin5kYIRCEJ+TISS1uCQeRDAwAAAj6SJ0"
    private var mRtcEngine: RtcEngine ?= null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
    }
    private lateinit var database: DatabaseReference
    private lateinit var chatAdapter: ChatAdapter
    private val REQUEST_IMAGE_CAPTURE = 101



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        // Initialize LinearLayoutManager
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true  // Optional: to start from the bottom
        recyclerView.layoutManager = layoutManager

        database = FirebaseDatabase.getInstance().reference.child("John_Cooper_Messages")
        chatAdapter = ChatAdapter(mutableListOf(), FirebaseDatabase.getInstance().reference.child("John_Cooper_Messages"))

        // Set the adapter to RecyclerView
        recyclerView.adapter = chatAdapter

        fetchChatMessages()
        val editText: TextView = findViewById(R.id.editText)
        val iconAfter3: ImageView = findViewById(R.id.iconAfter3)

        iconAfter3.setOnClickListener {
            val messageText = editText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val sender = "John Cooper"
                val timestamp = getCurrentTimeString()
                val chatMessage = ChatMessage(messageText, sender, timestamp)
                database.push().setValue(chatMessage)
                editText.text = ""
            }
        }


        fun initializeAndJoinChannel() {
            Log.d("YES", "Call Started")
            try {
                mRtcEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)
            } catch (_: Exception) {
            }
            mRtcEngine!!.joinChannel(TOKEN, CHANNEL, "", 0)
        }



        val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            //initializeAndJoinChannel();
        }



        whatsAppHandler = WhatsappHandler (this)
        val homeBtn: ImageView = findViewById(R.id.homeBtn)
        homeBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val searchBtn: ImageView = findViewById(R.id.searchBtn)
        searchBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
            finish()
        }

        val cameraBtn: ImageView = findViewById(R.id.cameraBtn)
        cameraBtn.setOnClickListener {
            dispatchTakePictureIntent()
        }

        val callBtn: ImageView = findViewById(R.id.callBtn)
        callBtn.setOnClickListener {
            val intent = Intent(this, AudioCallActivity::class.java)
            startActivity(intent)
            //startVoiceCall()
            //initializeAndJoinChannel()

        }

        val videoCallBtn: ImageView = findViewById(R.id.videoCallBtn)
        videoCallBtn.setOnClickListener {
            val intent = Intent(this, VideoCallActivity::class.java)
            startActivity(intent)
        }



        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        val gallery: ImageView = findViewById(R.id.iconBefore2)
        gallery.setOnClickListener {
            openGallery()
        }
        val videogallery: ImageView = findViewById(R.id.iconBefore1)
        videogallery.setOnClickListener {
            openVideoPicker()
        }


    }
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, PICK_VIDEO_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            uploadImageToFirebase(imageUri)
        } else if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val videoUri: Uri = data.data!!
            uploadVideoToFirebase(videoUri)
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadImageToFirebase(imageBitmap)
        }
    }

    private fun uploadVideoToFirebase(videoUri: Uri) {
        val videoName = database.push().key ?: ""
        val videoRef = database.child(videoName)

        val senderName = "John Cooper"
        val timestamp = getCurrentTimeString()

        // Get a reference to the Firebase storage location where you want to upload the video
        val storageRef = FirebaseStorage.getInstance().reference.child("videos").child(videoName)

        // Upload the video to Firebase Storage
        val uploadTask = storageRef.putFile(videoUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Get the download URL of the uploaded video
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Create a ChatMessage object with video URL and other metadata
                val chatMessage = ChatMessage("", senderName, timestamp, MessageType.VIDEO, "", uri.toString())

                // Store the ChatMessage object in the Realtime Database
                videoRef.setValue(chatMessage)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error uploading video: $exception")
                        Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting download URL: $exception")
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error uploading video: $exception")
            Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show()
        }
    }
    private fun uploadImageToFirebase(imageUri: Uri) {
        val imageName = database.push().key ?: ""
        val imageRef = database.child(imageName)

        val senderName = "John Cooper"
        val timestamp = getCurrentTimeString()

        // Get a reference to the Firebase storage location where you want to upload the image
        val storageRef = FirebaseStorage.getInstance().reference.child("images").child(imageName)

        // Upload the image to Firebase Storage
        val uploadTask = storageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Get the download URL of the uploaded image
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Create a ChatMessage object with image URL and other metadata
                val chatMessage = ChatMessage("", senderName, timestamp, MessageType.IMAGE, uri.toString())

                // Store the ChatMessage object in the Realtime Database
                imageRef.setValue(chatMessage)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error uploading image: $exception")
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting download URL: $exception")
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error uploading image: $exception")
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        val imageName = database.push().key ?: ""
        val imageRef = database.child(imageName)

        val senderName = "John Cooper"
        val timestamp = getCurrentTimeString()

        // Convert bitmap to byte array
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Get a reference to the Firebase storage location where you want to upload the image
        val storageRef = FirebaseStorage.getInstance().reference.child("images").child(imageName)

        // Upload the image to Firebase Storage
        val uploadTask = storageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Get the download URL of the uploaded image
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Create a ChatMessage object with image URL and other metadata
                val chatMessage = ChatMessage("", senderName, timestamp, MessageType.IMAGE, uri.toString())

                // Store the ChatMessage object in the Realtime Database
                imageRef.setValue(chatMessage)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error uploading image: $exception")
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Error getting download URL: $exception")
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error uploading image: $exception")
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }
    private fun fetchChatMessages() {
        // Add a ValueEventListener to retrieve data from Firebase Realtime Database
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatMessages = mutableListOf<ChatMessage>()
                // Loop through each child node in the "John_Cooper_Messages" table
                snapshot.children.forEach { dataSnapshot ->
                    val message = dataSnapshot.getValue(ChatMessage::class.java)
                    message?.let {
                        chatMessages.add(it)
                    }
                }
                // Update the UI with the fetched chat messages
                chatAdapter.updateMessages(chatMessages)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        Log.d("YES", "Call Ended")

    }
    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val PICK_VIDEO_REQUEST = 2

        private const val TAG = "YES"
    }
}




