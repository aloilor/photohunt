package com.example.macc_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Looper
import android.os.Handler
import android.view.WindowManager
import com.example.macc_project.auth.Login


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }, 3000)


    }
}