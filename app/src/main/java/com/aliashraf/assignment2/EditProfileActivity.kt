package com.aliashraf.assignment2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditProfileActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var countryEditText: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        nameEditText = findViewById(R.id.editText)
        emailEditText = findViewById(R.id.editText2)
        phoneNumberEditText = findViewById(R.id.editText3)
        cityEditText = findViewById(R.id.editText4)
        countryEditText = findViewById(R.id.editText5)

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userUid = user.uid
            val userRef = database.child("users").child(userUid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        // Populate EditText fields with user data
                        nameEditText.setText(user.name)
                        emailEditText.setText(user.email)
                        phoneNumberEditText.setText(user.phoneNumber)
                        cityEditText.setText(user.city)
                        countryEditText.setText(user.country)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                }
            })

            val updateProfileBtn: Button = findViewById(R.id.updateProfileBtn)
            updateProfileBtn.setOnClickListener {
                val newName = nameEditText.text.toString()
                val newEmail = emailEditText.text.toString()
                val newPhoneNumber = phoneNumberEditText.text.toString()
                val newCity = cityEditText.text.toString()
                val newCountry = countryEditText.text.toString()

                val updatedUser = User(newName, newEmail, newPhoneNumber, newCity, newCountry)
                userRef.setValue(updatedUser)
                    .addOnSuccessListener {
                        Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@EditProfileActivity, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        val backBtn: View = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }
}

data class User(
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var city: String = "",
    var country: String = ""
)
