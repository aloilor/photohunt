package com.example.macc_project.graphics

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet


class MyGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs) {

    private val renderer: CubeRenderer

    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8 , 8, 8, 8, 16, 4);

        renderer = CubeRenderer(context)
        setRenderer(renderer)

        renderMode = RENDERMODE_CONTINUOUSLY
    }



}