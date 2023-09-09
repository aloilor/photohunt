package com.example.macc_project.graphicsApp

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.macc_project.graphicsApp.CubeRenderer

class MyGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs) {

    private val renderer: CubeRenderer

    init {
        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8 , 8, 8, 8, 16, 4);

        // Create an instance of the CubeRenderer and set it as the renderer for this view
        renderer = CubeRenderer(context)
        setRenderer(renderer)

        // Render continuously
        renderMode = RENDERMODE_CONTINUOUSLY
    }



}