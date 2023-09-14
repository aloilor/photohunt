package com.example.macc_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.macc_project.databinding.ActivityHowToPlayBinding

class HowToPlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHowToPlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHowToPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homeButton.setOnClickListener {
            val it = Intent(this, HomePageActivity::class.java)
            startActivity(it)
        }
    }
}