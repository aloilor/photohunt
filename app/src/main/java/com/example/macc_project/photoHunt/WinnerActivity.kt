package com.example.macc_project.photoHunt

import android.app.ActivityManager
import android.content.Intent
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.macc_project.utilities.ExtraInfo
import com.example.macc_project.HomePageActivity
import com.example.macc_project.databinding.ActivityWinnerBinding
import com.example.macc_project.graphics.CubeRenderer

class WinnerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWinnerBinding
    private var glSurfaceView: GLSurfaceView? = null
    private var renderer: CubeRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWinnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.score.text = "${ExtraInfo.myScore.toString()}"

        binding.backButton.setOnClickListener {
            val it = Intent(this, HomePageActivity::class.java)
            startActivity(it)
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
        private const val TAG = "WinnerActivity"
    }
}