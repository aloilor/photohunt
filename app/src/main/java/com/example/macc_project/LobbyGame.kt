package com.example.macc_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.example.macc_project.databinding.ActivityLobbyGameBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ListenerRegistration

class LobbyGame : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var lobbyId: String = ""
    private lateinit var username: String
    private lateinit var binding: ActivityLobbyGameBinding
    private lateinit var listener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        username = intent.getStringExtra("email").toString()

        FirebaseApp.initializeApp(this)

        db.collection("lobbies")
            .whereEqualTo("statusGame", "waiting")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Join the first available lobby
                    val lobbyDoc = querySnapshot.documents[0]
                    val lobbyId = lobbyDoc.id
                    joinLobby(lobbyId)
                } else {
                    // If no lobby is available, create a new one
                    createNewLobby()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error checking for available lobbies: $exception")
            }

    }

    private fun createNewLobby() {
        // Generate a unique lobby ID
        lobbyId = generateUniqueLobbyId()

        // Create a new lobby in Firestore
        val lobby = hashMapOf(
            "lobby_id" to lobbyId,
            "player1" to username,
            "player2" to "",
            "statusGame" to "waiting"
        )

        db.collection("lobbies")
            .document(lobbyId)
            .set(lobby)
            .addOnSuccessListener {
                Log.d(TAG, "New lobby created with ID: $lobbyId")
                startGameIfReady(lobbyId)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error creating lobby: $exception")
            }
    }

    private fun joinLobby(lobbyId: String) {
        // Update player_2's name in the lobby document
        db.collection("lobbies")
            .document(lobbyId)
            .update("player2", username, "statusGame", "started")
            .addOnSuccessListener {
                Log.d(TAG, "Player 2 joined the lobby.")
                startGameIfReady(lobbyId)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error joining lobby: $exception")
            }
    }

    private fun startGameIfReady(lobbyId: String) {
        val docLobbyRef = db.collection("lobbies").document(lobbyId)

        // Listen for changes in the lobby document
        listener = docLobbyRef.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                return@EventListener
            }

            if (snapshot != null && snapshot.exists()) {
                val player1Name = snapshot.getString("player1") ?: ""
                val player2Name = snapshot.getString("player2") ?: ""
                val statusGame = snapshot.getString("statusGame") ?: "waiting"

                binding.player1TextView.text = "$player1Name"
                binding.player2TextView.text = "$player2Name"

                if (statusGame == "started" && !player2Name.isEmpty() && !player1Name.isEmpty()) {
                    Handler().postDelayed({
                        val intent = Intent(this, Hunt1Activity::class.java)
                        intent.putExtra("email", username)
                        intent.putExtra("lobbyId", lobbyId)
                        startActivity(intent)
                    }, 3000)
                }
            }
        })
    }



    private fun generateUniqueLobbyId(): String {
        return System.currentTimeMillis().toString()
    }


    companion object {
        private const val TAG = "LobbyActivity"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the listener when the activity is destroyed
        listener.remove()
    }
}