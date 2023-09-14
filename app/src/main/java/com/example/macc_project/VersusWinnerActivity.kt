package com.example.macc_project


import android.app.ActivityManager
import android.content.Intent
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.macc_project.databinding.ActivityVersusWinnerBinding
import android.os.Bundle
import android.util.Log
import com.example.macc_project.graphicsApp.CubeRenderer

class VersusWinnerActivity : AppCompatActivity(){

    private var glSurfaceView: GLSurfaceView? = null
    private var renderer: CubeRenderer? = null
    private lateinit var binding: ActivityVersusWinnerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVersusWinnerBinding.inflate(layoutInflater)
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

    }


    // Check if OpenGL ES 3.0 is supported
    private fun checkOpenGL3(): Boolean {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val info = am.deviceConfigurationInfo
        return info.reqGlEsVersion >= 0x30000
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView?.onPause()
    }


    companion object{
        private const val TAG = "VersusWinnerActivity"
    }

}