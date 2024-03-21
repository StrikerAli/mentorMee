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

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        fun signin(email: String, pass: String) {
            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) {task ->
                    if(task.isSuccessful) {
                        Log.d("TAG","Login:Sucess")
                        Toast.makeText(this, "Successfully LoggedIn", Toast.LENGTH_SHORT).show()
                        val user = mAuth.currentUser

                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)

                    } else {
                        Log.w("TAG","Login:failed", task.exception)
                        Toast.makeText(this,"Auth Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        // window.setFlags(android.R.attr.windowFullscreen, android.R.attr.windowFullscreen)

        val loginBtn: Button = findViewById(R.id.loginBtn)

        loginBtn.setOnClickListener {
            var email = findViewById<EditText>(R.id.editText).text.toString()
            var password = findViewById<EditText>(R.id.editText2).text.toString()

            signin(email,password)
        }

        val signupBtn: TextView = findViewById(R.id.signupBtn)
        signupBtn.paintFlags = signupBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        signupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        val forgotPassBtn: TextView = findViewById(R.id.forgotPassBtn)
        forgotPassBtn.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}