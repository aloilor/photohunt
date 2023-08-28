package com.example.macc_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //authentication
        auth = Firebase.auth
        val btnGoTo = findViewById<Button>(R.id.btnGoTo)
        val tvHomepage = findViewById<TextView>(R.id.tvHomepage)

        if (auth.currentUser != null) {
            val email = auth.currentUser?.email
            tvHomepage.text = "$email"
            btnGoTo.text = "Logout"
        } else {
            btnGoTo.text = "Login"
        }

        btnGoTo.setOnClickListener {
            if (auth.currentUser != null) {
                auth.signOut()
                tvHomepage.text = "Homepage"
                btnGoTo.text = "Login"
            } else {
                Intent(this, Login::class.java).also {
                    startActivity(it)
                }
            }
        }


    }
}