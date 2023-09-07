package com.example.macc_project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.macc_project.auth.Register
import com.example.macc_project.databinding.ActivityHomePageBinding

class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.CompeteButton.setOnClickListener {
            Intent(this, Hunt1Activity::class.java).also {
                startActivity(it)
            }
        }

        binding.VersusButton.setOnClickListener {
            Intent(this, LobbyGame::class.java).also {
                startActivity(it)
            }
        }

    }



}