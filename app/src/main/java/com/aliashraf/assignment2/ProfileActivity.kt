package com.aliashraf.assignment2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val mentorNameTextView: TextView = findViewById(R.id.Name)
        val mentorPositionTextView: TextView = findViewById(R.id.textView14)
        val mentorDescriptionTextView: TextView = findViewById(R.id.desc)
        val mentorProfileImageView: ImageView = findViewById(R.id.circleImageView)

        // Retrieve data from Intent extras
        val mentorName = intent.getStringExtra("mentor_name")
        val mentorPosition = intent.getStringExtra("mentor_position")
        val mentorDescription = intent.getStringExtra("mentor_description")
        val mentorImageUrl = intent.getStringExtra("mentor_image_url")

        // Set data to TextViews and ImageView
        mentorNameTextView.text = mentorName
        mentorPositionTextView.text = mentorPosition
        mentorDescriptionTextView.text = mentorDescription
        // Load mentor image using Picasso
        Picasso.get().load(mentorImageUrl).into(mentorProfileImageView)

        // Button to open BookSessionActivity
        val bookSessionBtn: Button = findViewById(R.id.bookSessionBtn)
        bookSessionBtn.setOnClickListener {
            val intent = Intent(this, BookSessionActivity::class.java).apply {
                putExtra("mentor_name", mentorName)
                putExtra("mentor_position", mentorPosition)
                putExtra("mentor_description", mentorDescription)
                putExtra("mentor_image_url", mentorImageUrl)
            }
            startActivity(intent)
        }

        // Button to open ReviewActivity
        val dropReviewBtn: Button = findViewById(R.id.dropReviewBtn)
        dropReviewBtn.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java).apply {
                putExtra("mentor_name", mentorName)
                putExtra("mentor_position", mentorPosition)
                putExtra("mentor_description", mentorDescription)
                putExtra("mentor_image_url", mentorImageUrl)
            }
            startActivity(intent)
        }

        val joinCommunityBtn: Button = findViewById(R.id.joinCommunityBtn)

        joinCommunityBtn.setOnClickListener {
            val intent = Intent(this, CommunityActivity::class.java)
            startActivity(intent)
        }

        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }
}