package com.aliashraf.assignment2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class ReviewActivity : AppCompatActivity() {
    private lateinit var mentorName: String
    private lateinit var mentorImageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // Get mentor name and image URL from intent extras
        mentorName = intent.getStringExtra("mentor_name") ?: ""
        mentorImageUrl = intent.getStringExtra("mentor_image_url") ?: ""

        // Set mentor name and image
        val mentorNameTextView: TextView = findViewById(R.id.Name)
        val mentorProfileImageView: ImageView = findViewById(R.id.circleImageView)
        mentorNameTextView.text = mentorName
        Picasso.get().load(mentorImageUrl).into(mentorProfileImageView)

        // Handle click on the back button
        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        // Handle click on the submit button
        val submitBtn: View = findViewById(R.id.submitBtn)
        submitBtn.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        val user = FirebaseAuth.getInstance().currentUser
        val userMail = user?.email ?: ""
        val ratingBar: RatingBar = findViewById(R.id.ratingBar)
        val rating = ratingBar.rating
        val experienceText: TextView = findViewById(R.id.reviewTxt)
        val experience = experienceText.text.toString().trim()

        // Ensure a rating is selected
        if (rating == 0f) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure experience text is not empty
        if (experience.isEmpty()) {
            Toast.makeText(this, "Please provide your experience", Toast.LENGTH_SHORT).show()
            return
        }

        // Write review data to Firebase Realtime Database
        val database = FirebaseDatabase.getInstance()
        val reviewsRef = database.getReference("reviews")
        val reviewId = reviewsRef.push().key ?: ""

        val reviewData = hashMapOf(
            "userId" to userMail,
            "mentorName" to mentorName,
            "experience" to experience,
            "rating" to rating
        )

        reviewsRef.child(reviewId).setValue(reviewData)
            .addOnSuccessListener {
                Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to submit review: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
