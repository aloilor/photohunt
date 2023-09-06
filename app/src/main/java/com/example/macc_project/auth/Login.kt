package com.example.macc_project

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.example.macc_project.auth.GithubSignIn
import com.example.macc_project.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var  binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
        val user = Firebase.auth.currentUser

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT)
                .show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val it = Intent(this, LobbyGame::class.java)
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