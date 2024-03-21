package com.aliashraf.assignment2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.aliashraf.assignment2.AddMentorActivity.Companion.CHANNEL_ID
import com.squareup.picasso.Picasso


class BookSessionActivity : AppCompatActivity() {
    private lateinit var buttonContainer: LinearLayout
    private lateinit var selectedDate: String
    private lateinit var selectedHour: String
    private var messageId = 0
    private var notificationId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_session)

        val mentorNameTextView: TextView = findViewById(R.id.Name)

        val mentorProfileImageView: ImageView = findViewById(R.id.circleImageView)

        // Retrieve data from Intent extras
        val mentorName = intent.getStringExtra("mentor_name")

        val mentorImageUrl = intent.getStringExtra("mentor_image_url")

        // Set data to TextViews and ImageView
        mentorNameTextView.text = mentorName
        // Load mentor image using Picasso
        Picasso.get().load(mentorImageUrl).into(mentorProfileImageView)

        buttonContainer = findViewById(R.id.buttonContainer)

        // Generate buttons for all 24 time slots
        for (i in 0 until 24) {
            val hour = String.format("%02d", i) // Format hour with leading zero if needed
            val time = "$hour:00" // Format time as HH:00 AM/PM

            val button = AppCompatButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = time
                setOnClickListener { onButtonClicked(this) }
                setBackgroundResource(R.drawable.appointment_time_btn)
                setTextColor(ContextCompat.getColor(this@BookSessionActivity, R.color.black))
            }

            buttonContainer.addView(button)
        }

        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // month is 0-based, so add 1 for the actual month
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            Log.d("ButtonClicked", "Date $selectedDate is selected")

        }

        val bookAppointmentButton: View = findViewById(R.id.bookAppointment)
        bookAppointmentButton.setOnClickListener {
            bookAppointment()
        }
    }

    fun onButtonClicked(view: View) {
        val buttonText = (view as AppCompatButton).text.toString()
        selectedHour = buttonText
        Log.d("ButtonClicked", "Hour button $selectedHour is pressed")

    }


    private fun bookAppointment() {
        if (::selectedDate.isInitialized && ::selectedHour.isInitialized) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val bookeremail = currentUser?.email ?: "Unknown Mentor"
            val mentorName = intent.getStringExtra("mentor_name") ?: ""
            val databaseReference = FirebaseDatabase.getInstance().reference.child("bookings")
            val appointmentDetails = Appointment(selectedDate, selectedHour, bookeremail, mentorName)


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

            val key = databaseReference.push().key
            key?.let {
                databaseReference.child(it).setValue(appointmentDetails)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
                        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.notifications)
                            .setContentTitle("Appointment booked successfully")
                            .setContentText("Your appointment has been successfully booked.")
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                        // Show the notification
                        notificationManager.notify(notificationId, builder.build())
                        Log.d("Success", "Notification sent successfully!")
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to book appointment!", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please select both date and time.", Toast.LENGTH_SHORT).show()
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



data class Appointment(val date: String, val time: String, val bookeremail: String, val mentorname: String)

