package com.aliashraf.assignment2

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SignupActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance();
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

         fun signup(name: String, email: String, password: String, city: String, country: String, phoneNumber: String, usersRef: DatabaseReference) {
            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        Log.d("TAG","SignUp: Success")
                        Toast.makeText(this, "Successfully Signup", Toast.LENGTH_SHORT).show()
                        val user = mAuth.currentUser

                        // Update additional user information
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Log.d("TAG", "User profile updated.")
                                }
                            }

                        // Save additional user data to Realtime Database
                        val userId = user!!.uid
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "city" to city,
                            "country" to country,
                            "phoneNumber" to phoneNumber
                        )
                        usersRef.child(userId).setValue(userData)
                            .addOnSuccessListener {
                                Log.d("TAG", "User data successfully written to Realtime Database!")
                                val intent = Intent(this, VerifyPhoneActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener {
                                Log.e("TAG", "Error writing user data to Realtime Database")
                                Toast.makeText(this, "Signup Failed", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.w("TAG","SignUp:failed", task.exception)
                        Toast.makeText(this,"Signup Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        val signupBtn: Button = findViewById(R.id.signupBtn)

        signupBtn.setOnClickListener {
            var email = findViewById<EditText>(R.id.editText2).text.toString()
            var password = findViewById<EditText>(R.id.editText6).text.toString()
            var name = findViewById<EditText>(R.id.editText).text.toString()
            var city = findViewById<EditText>(R.id.editText5).text.toString()
            var country = findViewById<EditText>(R.id.editText4).text.toString()
            var number = findViewById<EditText>(R.id.editText3).text.toString()

            signup(name,email,password,city,country,number,usersRef)
        }

        val loginBtn: TextView = findViewById(R.id.loginBtn)
        loginBtn.paintFlags = loginBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        loginBtn.setOnClickListener {
            finish()
        }
    }
}