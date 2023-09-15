package com.example.macc_project.graphics

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CubeRenderer(val context: Context) : GLSurfaceView.Renderer {

    var cube: Cube? = null

    // Model-View-Projection matrix
    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)
    private var angle = 0f
    private var x = 0f
    private var y = 0f

    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        // Set the clear buffer color like dark theme
        GLES30.glClearColor(0.11f, 0.11f, 0.11f, 1.0f)
        cube = Cube(context)
    }

    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        // Set the viewport
        GLES30.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height

        // Projection Matrix
        Matrix.perspectiveM(projectionMatrix, 0, 53.13f, aspect, 1f, 50f)
    }

    override fun onDrawFrame(glUnused: GL10) {
        // Clear the color and depth buffers
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Enable depth testing
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Create a rotation and translation for the cube
        Matrix.setIdentityM(rotationMatrix, 0)

        // Move the cube up/down and left/right
        Matrix.translateM(rotationMatrix, 0, x, y, 0f)

        // Set rotation angle and axis (x, y, z) for the cube
        Matrix.rotateM(rotationMatrix, 0, angle, 0.4f, 1.0f, 0.6f)

        // Combine the model with the view matrix
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, rotationMatrix, 0)

        // Combine the model-view with the projection matrix
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        // Draw the cube with the calculated MVP matrix
        cube!!.draw(mvpMatrix)

        // Update the rotation angle for animation
        angle += 0.4f
    }

    // Methods to set cube translation
    fun setTranslation(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
}
