package com.example.macc_project.auth

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.example.macc_project.utilities.ExtraInfo
import com.example.macc_project.HomePageActivity
import com.example.macc_project.R
import com.example.macc_project.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var  binding: ActivityLoginBinding


    /*public override fun onStart() {
       super.onStart()
        // Check if user is signed in
        val currentUser = auth.currentUser
        if(currentUser != null){
            Toast.makeText(
                baseContext, "User already logged.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        Log.w("debugger", "accessing login" )


        auth = Firebase.auth


        binding.signupText.setOnClickListener {
            Intent(this, Register::class.java).also {
                startActivity(it)
            }
        }
       binding.LoginButton.setOnClickListener {
            loginUser()
        }

        binding.googleButton.setOnClickListener {
            goToGoogleSignIn()
        }
        binding.githubButton.setOnClickListener {
           goToGithubSignIn()
        }
    }

    private fun loginUser() {
        val email = findViewById<EditText>(R.id.LoginUsername).text.toString()
        val password = findViewById<EditText>(R.id.LoginPassword).text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT)
                .show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    ExtraInfo.setEmail(email)

                    val it = Intent(this, HomePageActivity::class.java)
                    it.putExtra("email", email)
                    startActivity(it)


                    Toast.makeText(
                        baseContext,
                        "Authentication Success.",
                        Toast.LENGTH_SHORT,
                    ).show()


                } else {
                    // If sign in fails,
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }


    }
    private fun goToGoogleSignIn(){
        val intent = Intent(this, GoogleSignIn::class.java)
        startActivity(intent)
    }
    private fun goToGithubSignIn(){
        val intent = Intent(this, GithubSignIn::class.java)
        startActivity(intent)
    }
}