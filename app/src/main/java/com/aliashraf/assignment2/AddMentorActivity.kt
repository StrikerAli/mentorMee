package com.aliashraf.assignment2

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddMentorActivity : AppCompatActivity() {
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectedImageUri: Uri
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var availableEditText: EditText
    private lateinit var PostionEditText: EditText
    private lateinit var storageReference: FirebaseStorage
    private var notificationId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_mentor)

        val uploadBtn: Button = findViewById(R.id.submitBtn)
        val openGallery : ImageView = findViewById(R.id.iconCam)

        openGallery.setOnClickListener {
            openGalleryForImage()
        }

        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        selectedImageUri = uri

                    }
                }
            }
        uploadBtn.setOnClickListener {
            storageReference = FirebaseStorage.getInstance()
            nameEditText = findViewById(R.id.editText)
            descriptionEditText = findViewById(R.id.editText2)
            availableEditText = findViewById(R.id.editText56)
            PostionEditText = findViewById(R.id.editText5)
            uploadDataToFirebase()

        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun uploadDataToFirebase() {
        if (!::selectedImageUri.isInitialized) {
            Toast.makeText(this, "Please select an image from the gallery", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val name = nameEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val available = availableEditText.text.toString()
        val position = PostionEditText.text.toString()

        val storageRef = storageReference.reference
        val imageName = UUID.randomUUID().toString()
        val imageRef = storageRef.child("mentor_images").child("$imageName.jpg")
        Log.d("Image path", selectedImageUri.toString())

        val notificationManager = ContextCompat.getSystemService(this, NotificationManager::class.java) as NotificationManager
        createNotificationChannel()

        val intent = Intent(this, HomeActivity::class.java).apply {
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        imageRef.putFile(selectedImageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    val database = FirebaseDatabase.getInstance()
                    val mentorsRef = database.getReference("mentors")
                    val mentorId = mentorsRef.push().key ?: ""

                    val mentorData = hashMapOf(
                        "name" to name,
                        "description" to description,
                        "available" to available,
                        "position" to position,
                        "imageUrl" to imageUrl
                    )

                    mentorsRef.child(mentorId).setValue(mentorData)
                        .addOnSuccessListener {
                            Log.d("TAG","Data upload successfully")
                            Toast.makeText(this, "Data uploaded sucessfully", Toast.LENGTH_SHORT).show()
                            val builder = NotificationCompat.Builder(this,
                                CHANNEL_ID
                            )
                                .setSmallIcon(R.drawable.notifications)
                                .setContentTitle("Mentor added successfully")
                                .setContentText("Your mentor has been successfully added.")
                                .setContentIntent(pendingIntent)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                            // Show the notification
                            notificationManager.notify(notificationId, builder.build())
                            Log.d("Success", "Notification sent successfully!")



                        }
                        .addOnFailureListener {
                            Log.e("TAG","Error uploading data")
                            Toast.makeText(this, "Data uploaded failed", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Picture selection failed", Toast.LENGTH_SHORT).show()
                Log.e("TAG","Image Selection Failed", exception)
            }
    }
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    companion object {
        const val CHANNEL_ID = "Your_Channel_ID"
        const val NOTIFICATION_ID = 123 // You can use any integer value for your notification ID
    }
}