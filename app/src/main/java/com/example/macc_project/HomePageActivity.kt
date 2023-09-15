package com.example.macc_project

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.macc_project.auth.Login
import com.example.macc_project.databinding.ActivityHomePageBinding
import com.example.macc_project.graphics.CubeRenderer
import com.example.macc_project.photoHunt.Hunt1Activity
import com.example.macc_project.photoHunt.LobbyGame
import com.example.macc_project.photoHunt.ScoreboardActivity
import com.example.macc_project.utilities.ExtraInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private var glSurfaceView: GLSurfaceView? = null
    private var renderer: CubeRenderer? = null
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private val usersCollection = db.collection("users")
    private var username:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        //keeping clean the variables
        ExtraInfo.myScore = 0
        ExtraInfo.myLevel = 1
        ExtraInfo.myLobbyID = "1"
        ExtraInfo.actualMilliseconds = 0

        //Alert dialog to logout
        val logoutDialog = AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Do you want to log out?")
            .setPositiveButton("Logout"){_,_ ->
                FirebaseAuth.getInstance().signOut()
                Intent(this, Login::class.java).also {
                    startActivity(it)
                }
            }
            .setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }.create()


        // Retrieve the user document
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // retrieve the username from the document
                    username = document.getString("username")
                    if (username != null) {
                        ExtraInfo.setUsername(username!!)
                        binding.username.text = username
                    } else {
                        Log.w(TAG,"Username doesn't exist")
                    }
                } else {
                    Log.w(TAG,"Document doesn't exist")
                }

            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting user document", exception)
            }

        if (checkOpenGL3()) {
            // Create a GLSurfaceView with OpenGL ES 3.0 context
            glSurfaceView = GLSurfaceView(this)
            glSurfaceView?.setEGLContextClientVersion(3)

            // Create a CubeRenderer and set it as the renderer
            renderer = CubeRenderer(this)
            glSurfaceView?.setRenderer(renderer)
            val layout = binding.cubeLayout
            layout.addView(glSurfaceView)
        } else {
            Log.d(TAG, "OpenGL ES 3.0 not supported on device")
            finish()
        }


        binding.CompeteButton.setOnClickListener {
            Intent(this, Hunt1Activity::class.java).also {
                startActivity(it)
            }
        }

        binding.VersusButton.setOnClickListener {
            Intent(this, LobbyGame::class.java).also {
                it.putExtra("username",username)
                startActivity(it)
            }
        }
        binding.ScoreboardButton.setOnClickListener {
            Intent(this, ScoreboardActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.HowToPlayButton.setOnClickListener {
            Intent(this, HowToPlayActivity::class.java).also {
                startActivity(it)
            }
        }
        binding.logoutButton.setOnClickListener{
            logoutDialog.show()

        }

    }
    override fun onResume() {
        super.onResume()
        glSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView?.onPause()
    }

    // Check if OpenGL ES 3.0 is supported
    private fun checkOpenGL3(): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x30000
    }

companion object{
    private const val TAG = "HomePageActivity"
}

}