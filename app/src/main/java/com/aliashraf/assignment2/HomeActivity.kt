package com.aliashraf.assignment2

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import androidx.core.app.NotificationManagerCompat


data class Mentor(
    val available: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val name: String = "",
    val position: String = ""
) {
    // No-argument constructor
    constructor() : this("", "", "", "", "")
}

class ScreenshotObserver(private val context: Context, handler: Handler) :
    ContentObserver(handler) {

    private val contentResolver: ContentResolver = context.contentResolver
    private val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
    private val notificationChannelId = "ScreenshotChannel"
    private val notificationId = 1001

    init {
        createNotificationChannel()
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        // Check if the content change is a screenshot
        if (isScreenshot(uri)) {
            //Toast.makeText(context, "Screenshot Taken!", Toast.LENGTH_SHORT).show()
            showNotification("Screenshot taken!", "You've taken a screenshot.")
        }
    }

    private fun isScreenshot(uri: Uri?): Boolean {
        return uri != null && uri.toString().startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Screenshot Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notification channel for screenshot events"
                lightColor = Color.BLUE
                enableLights(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        Toast.makeText(context, "Screenshot Taken!", Toast.LENGTH_SHORT).show()
        val notificationBuilder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}


class HomeActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var horizontalScrollView: HorizontalScrollView
    private lateinit var linearLayout: LinearLayout
    var isHeartFilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        registerScreenshotObserver()


        // window.setFlags(android.R.attr.windowFullscreen, android.R.attr.windowFullscreen)
        databaseReference = FirebaseDatabase.getInstance().reference.child("mentors")
        horizontalScrollView = findViewById(R.id.horizontalScrollView3)
        linearLayout = findViewById(R.id.yes_Scroll_View)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (mentorSnapshot in dataSnapshot.children) {
                    val mentor = mentorSnapshot.getValue(Mentor::class.java)
                    if (mentor != null) {
                        if (mentor.available == "Available")
                            mentor?.let { createMentorCard(it) }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        val searchBtn: ImageView = findViewById(R.id.searchBtn)
        searchBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
            finish()
        }


        val chatBtn: ImageView = findViewById(R.id.chatBtn)
        chatBtn.setOnClickListener {
            val intent = Intent(this, AllChatsActivity::class.java)
            startActivity(intent)
            finish()
        }

        val accountBtn: ImageView = findViewById(R.id.accountBtn)
        accountBtn.setOnClickListener {
            val intent = Intent(this, MyProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val notificationBtn: ImageView = findViewById(R.id.notificationBtn)
        notificationBtn.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        val addMentorBtn: ImageView = findViewById(R.id.addMentorBtn)
        addMentorBtn.setOnClickListener {
            val intent = Intent(this, AddMentorActivity::class.java)
            startActivity(intent)
        }

        val mentor1: CardView = findViewById(R.id.mentor1)
        mentor1.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }




        /*        val accountBtn: ImageView = findViewById(R.id.accountBtn)
                accountBtn.setOnClickListener {
                    val intent = Intent(this, ::class.java)
                    startActivity(intent)
                    finish()
                }*/


    }
    private fun createMentorCard(mentor: Mentor) {
        val cardView = CardView(this)
        val cardParams = LinearLayout.LayoutParams(
            resources.getDimension(R.dimen.card_width).toInt(),
            resources.getDimension(R.dimen.card_height).toInt()
        )
        // Set margins for the cardView
        cardParams.setMargins(
            resources.getDimension(R.dimen.card_margin).toInt(),
            0,
            resources.getDimension(R.dimen.card_margin).toInt(),
            0
        )
        cardView.layoutParams = cardParams
        cardView.radius = resources.getDimension(R.dimen.card_corner_radius)
        cardView.cardElevation = resources.getDimension(R.dimen.card_elevation)

        val constraintLayout = ConstraintLayout(this)
        cardView.addView(constraintLayout)

        // Load mentor image
        val imageView = ImageView(this)
        imageView.id = View.generateViewId()
        val imageViewParams = ConstraintLayout.LayoutParams(
            resources.getDimension(R.dimen.image_width).toInt(),
            resources.getDimension(R.dimen.image_height).toInt()
        )
        // Set image constraints and margins
        imageViewParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        imageViewParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        imageViewParams.marginStart = resources.getDimension(R.dimen.image_margin).toInt()
        imageViewParams.topMargin = resources.getDimension(R.dimen.top_margin).toInt()
        imageView.layoutParams = imageViewParams
        Picasso.get().load(mentor.imageUrl).into(imageView)
        constraintLayout.addView(imageView)

        // Set mentor name TextView
        val textViewName = TextView(this)
        textViewName.id = View.generateViewId()
        val nameParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        nameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        nameParams.topToBottom = imageView.id
        nameParams.setMargins(30, 0, 0, 0)
        textViewName.layoutParams = nameParams
        textViewName.text = mentor.name
        textViewName.setTextColor(Color.parseColor("#008B8B"))
        textViewName.setTypeface(null, Typeface.BOLD)
        constraintLayout.addView(textViewName)

        // Set mentor position TextView
        val textViewPosition = TextView(this)
        textViewPosition.id = View.generateViewId()
        val positionParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        positionParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        positionParams.topToBottom = textViewName.id
        positionParams.setMargins(33, 0, 0, 0)
        textViewPosition.layoutParams = positionParams
        textViewPosition.text = mentor.position
        textViewPosition.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
        constraintLayout.addView(textViewPosition)

        // Set mentor description TextView
        val textViewDescription = TextView(this)
        textViewDescription.id = View.generateViewId()
        val descriptionParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        descriptionParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        descriptionParams.topToBottom = textViewPosition.id
        descriptionParams.setMargins(45, 0, 0, 0)
        textViewDescription.layoutParams = descriptionParams
        textViewDescription.text = mentor.available
        textViewDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f)
        textViewDescription.setTypeface(null, Typeface.BOLD)
        textViewDescription.setTextColor(Color.parseColor("#44af08"))
        constraintLayout.addView(textViewDescription)

        // Set onClickListener for cardView
        cardView.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("mentor_name", mentor.name)
                putExtra("mentor_position", mentor.position)
                putExtra("mentor_description", mentor.description)
                putExtra("mentor_image_url", mentor.imageUrl)
            }
            startActivity(intent)
        }

        val textViewName4 = TextView(this)
        textViewName4.id = View.generateViewId()
        val nameParams4 = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        nameParams4.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        nameParams4.topToBottom = imageView.id // Set top constraint to the bottom of imageView
        textViewName4.layoutParams = nameParams4
        textViewName4.text = "1500$/S"
        nameParams4.setMargins(300, 5, 0, 0) // Set margins (left, top, right, bottom)

        textViewName4.setTextColor(Color.parseColor("#ffd336")) // Set text color to red
        textViewName4.setTypeface(null, Typeface.BOLD) // Set text style to bold
        textViewName4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f) // Set text size to 14sp
        constraintLayout.addView(textViewName4)

        val imageView2 = ImageView(this)
        imageView2.id = View.generateViewId()
        val imageViewParams2 = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        imageViewParams2.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        imageViewParams2.topToBottom = textViewName4.id // Set top constraint to the bottom of textViewName4
        imageView2.layoutParams = imageViewParams2
        imageView2.setImageResource(R.drawable.ic_heart_empty) // Set the image resource for the ImageView
        imageViewParams2.setMargins(350, 10, 0, 0) // Set margins (left, top, right, bottom)

        constraintLayout.addView(imageView2)
        imageView2.setOnClickListener {
            // Toggle the boolean value
            isHeartFilled = !isHeartFilled

            // Determine which image resource to set based on the boolean value
            val newImageResource = if (isHeartFilled) {
                R.drawable.ic_heart_fill // Use this image resource when isHeartFilled is true
            } else {
                R.drawable.ic_heart_empty // Use this image resource when isHeartFilled is false
            }

            // Set the new image resource for imageView2
            imageView2.setImageResource(newImageResource)
        }

        linearLayout.addView(cardView)
    }
    private fun registerScreenshotObserver() {
        val screenshotObserver = ScreenshotObserver(this, Handler())
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            screenshotObserver
        )
    }

}