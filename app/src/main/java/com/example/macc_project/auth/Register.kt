package com.example.macc_project.auth

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.macc_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        val registerButton = findViewById<Button>(R.id.RegisterButton)
        registerButton.setOnClickListener {
            registerUser()
        }
        val loginButton = findViewById<TextView>(R.id.signinText)
        loginButton.setOnClickListener {
            goToLogin()
        }


    }


    private fun registerUser() {
        val email = findViewById<EditText>(R.id.RegisterEmail).text.toString()
        val username = findViewById<EditText>(R.id.RegisterUsername).text.toString()
        val password = findViewById<EditText>(R.id.RegisterPassword).text.toString()
        val confPsw = findViewById<EditText>(R.id.RegisterConfirm).text.toString()

        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || confPsw.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if(password != confPsw){
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT)
                .show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val userFirebase = auth.currentUser
                    if(userFirebase != null) {
                        addUserToDB(userFirebase,username)
                    }

                    Toast.makeText(
                        baseContext,
                        "Registration Success.",
                        Toast.LENGTH_SHORT,
                    ).show()

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        "Registration failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }


    }
    private fun addUserToDB(currentUser:FirebaseUser, username : String){
        val db = Firebase.firestore

        val user = hashMapOf(
            "email" to currentUser.email,
            "username" to username,
            "points" to 0
        )

        db.collection("users").document(currentUser.uid)
            .set(user)
            .addOnSuccessListener {
                goToLogin()
                Toast.makeText(
                    baseContext, "User correctly registered",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error during the registration", e) }
    }

    private fun goToLogin(){
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
}