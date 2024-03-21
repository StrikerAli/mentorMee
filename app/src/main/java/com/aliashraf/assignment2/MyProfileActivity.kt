package com.aliashraf.assignment2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class MyProfileActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var circleImageView8: ImageView
    private lateinit var backgroundImageView: ImageView
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        circleImageView8 = findViewById(R.id.circleImageView8)
        backgroundImageView = findViewById(R.id.background)

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        // Load the user's image from Firebase Storage and display it in circleImageView8
        currentUserEmail?.let { email ->
            databaseReference.child(email.replace(".", "_")).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val imageUrl = dataSnapshot.child("imageUrl").getValue(String::class.java)
                    imageUrl?.let {
                        Picasso.get().load(it).into(circleImageView8)
                    }

                    val coverImageUrl = dataSnapshot.child("coverImageUrl").getValue(String::class.java)
                    coverImageUrl?.let {
                        Picasso.get().load(it).into(backgroundImageView)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
        }

        // Your existing code...
        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        val editBtn2: ImageView = findViewById(R.id.editBtn2)
        editBtn2.setOnClickListener {
            // Open gallery for profile image
            openGallery(PICK_IMAGE_REQUEST)
        }

        val editBtn3: ImageView = findViewById(R.id.editBtn)
        editBtn3.setOnClickListener {
            // Open gallery for cover image
            openGallery(PICK_IMAGE_REQUEST + 1)
        }
        val searchBtn: ImageView = findViewById(R.id.editBtn3)
        searchBtn.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ((requestCode == PICK_IMAGE_REQUEST || requestCode == PICK_IMAGE_REQUEST + 1) && resultCode == Activity.RESULT_OK && data != null) {
            // Get the image URI
            val imageUri = data.data
            // Upload the image to Firebase Storage and store its URL in Firebase Database
            if (requestCode == PICK_IMAGE_REQUEST) {
                uploadImageToFirebase(imageUri, "imageUrl")
            } else if (requestCode == PICK_IMAGE_REQUEST + 1) {
                uploadImageToFirebase(imageUri, "coverImageUrl")
            }
        }
    }

    private fun openGallery(requestCode: Int) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, requestCode)
    }

    private fun uploadImageToFirebase(imageUri: Uri?, imageUrlKey: String) {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        if (currentUserEmail != null && imageUri != null) {
            val imageRef = FirebaseStorage.getInstance().reference.child("images/${currentUserEmail.hashCode()}")

            // Upload image to Firebase Storage
            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL of the image
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageURL = uri.toString()

                        // Update user's information in Firebase Realtime Database
                        val userUpdates = hashMapOf<String, Any>(
                            imageUrlKey to imageURL
                        )
                        databaseReference.child(currentUserEmail.replace(".", "_")).updateChildren(userUpdates)
                            .addOnSuccessListener {
                                // Image URL stored successfully
                            }
                            .addOnFailureListener {
                                // Handle error
                            }
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
        }
    }
}

