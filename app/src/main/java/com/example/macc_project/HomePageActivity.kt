package com.example.macc_project

import android.app.ActivityManager
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.macc_project.databinding.ActivityHomePageBinding
import com.example.macc_project.graphicsApp.CubeRenderer



class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private var glSurfaceView: GLSurfaceView? = null
    private var renderer: CubeRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        if (detectOpenGLES30()) {
            // Create a GLSurfaceView with OpenGL ES 3.0 context
            glSurfaceView = GLSurfaceView(this)
            glSurfaceView?.setEGLContextClientVersion(3)

            // Create a CubeRenderer and set it as the renderer
            renderer = CubeRenderer(this)
            glSurfaceView?.setRenderer(renderer)
            val layout = binding.linearLayout
            layout.addView(glSurfaceView)
        } else {
            // OpenGL ES 3.0 not supported on the device
            Log.e("OpenGLCube", "OpenGL ES 3.0 not supported on device")
            finish()
        }




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
        binding.ScoreboardButton.setOnClickListener {
            Intent(this, ScoreboardActivity::class.java).also {
                startActivity(it)
            }
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
    private fun detectOpenGLES30(): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x30000
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }


}