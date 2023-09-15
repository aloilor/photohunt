package com.example.macc_project.photoHunt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.macc_project.R
import com.example.macc_project.databinding.ActivityScoreboardBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ScoreboardActivity : AppCompatActivity() {
    data class User(
        val email: String = "",
        val username: String = "",
        val points: Int = 0
    )

    private val usersList = mutableListOf<User>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScoreboardAdapter
    private lateinit var  binding: ActivityScoreboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoreboard)
        binding = ActivityScoreboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.rvScoreboard
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ScoreboardAdapter(usersList)
        recyclerView.adapter = adapter

        // Fetch users data from Firestore
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.orderBy("points", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    usersList.add(user)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to get ranking users: $exception")
            }
    }
    companion object{
        const val TAG = "ScoreboardActivity"
    }
}