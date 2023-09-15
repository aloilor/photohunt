package com.example.macc_project.graphics

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import com.example.macc_project.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer



class Cube(private val context: Context ){
    /*var vShaderStr = """#version 300 es
        uniform mat4 uMatrix;     
        in vec4 vPosition;           
        void main()                  
        {                            
           gl_Position = uMatrix * vPosition;  
        }                            
        """

    //fragment shader code.
    var fShaderStr = """#version 300 es		 			          	
        precision mediump float;					  	
        uniform vec4 vColor;	 			 		  	
        out vec4 fragColor;	 			 		  	
        void main()                                  
        {                                            
          fragColor = vec4(1.0,0.0,0.0,1.0);                    	
        }                                            
        """
   *///vertex shader
    var vShaderStr =
        """
        #version 300 es 
        precision mediump float;
        

        uniform mat4 uMatrix;     
        in vec4 vPosition;  
        in vec2 aTexCoord;
        out vec2 vTexCoord;
        void main()                  
        {                            
           gl_Position = uMatrix * vPosition; 
            vTexCoord = aTexCoord;
        }                            
        """

    //fragment shader
    var fShaderStr = """
        #version 300 es		 			          	
        precision mediump float;
        					  	
        in vec2 vTexCoord;
        uniform sampler2D uTexture; 			 		  	
        out vec4 fragColor;	 			 		  	
        void main()                                  
        {                                            
          fragColor = texture(uTexture, vTexCoord);                  	
        }                                            
        """

    private var program: Int = 0
    private var matrixHandle = 0
    private val vertices: FloatBuffer
    private val textureHandle: Int
    private val texCoordHandle: Int
    private val texCoords: FloatBuffer
    private val indices: ShortBuffer
    private val textureIds = IntArray(6)

    var size = 0.3f

    private var TAG = "Cube"

    // Define the vertices for the cube
    private val verticesData =
        floatArrayOf(
            // Front face
            -size, -size, size,  // 0
            size, -size, size,   // 1
            size, size, size,    // 2
            -size, size, size,   // 3

            // Back face
            -size, -size, -size, // 4
            size, -size, -size,  // 5
            size, size, -size,   // 6
            -size, size, -size,  // 7

            // Top face
            -size, size, size,   // 8
            size, size, size,    // 9
            size, size, -size,   // 10
            -size, size, -size,  // 11

            // Bottom face
            -size, -size, size,  // 12
            size, -size, size,   // 13
            size, -size, -size,  // 14
            -size, -size, -size, // 15

            // Right face
            size, -size, size,   // 16
            size, -size, -size,  // 17
            size, size, -size,   // 18
            size, size, size,    // 19

            // Left face
            -size, -size, size,  // 20
            -size, -size, -size, // 21
            -size, size, -size,  // 22
            -size, size, size    // 23
        )

    // Define texture coordinates for each vertex
    private val texCoordsData =
        floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,

            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
        )

    // Define indices
    private val indicesData =
        shortArrayOf(
            0, 1, 2, 0, 2, 3,  // Front
            4, 5, 6, 4, 6, 7,  // Back
            8, 9, 10, 8, 10, 11,  //Top
            12, 13, 14, 12, 14, 15,  //Bottom
            16, 17, 18, 16, 18, 19,  //Right
            20, 21, 22, 20, 22, 23   //Left
        )


    init {

        vertices = ByteBuffer
            .allocateDirect(verticesData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(verticesData)
        vertices.position(0)

        texCoords = ByteBuffer
            .allocateDirect(texCoordsData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texCoordsData)
        texCoords.position(0)

        indices = ByteBuffer
            .allocateDirect(indicesData.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indicesData)
        indices.position(0)

        // Load shaders and create program
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vShaderStr)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fShaderStr)

        program = GLES30.glCreateProgram()
        if (program == 0) {
            Log.e(TAG, "Cube.kt : error on glCreateProgram?")
        }
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, fragmentShader)

        GLES30.glBindAttribLocation(program, 0, "vPosition")

        GLES30.glLinkProgram(program)

        // Check shader program status
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES30.GL_TRUE) {
            Log.e(TAG, "Shader program linking failed")
            GLES30.glDeleteProgram(program)
        }
        matrixHandle = GLES30.glGetUniformLocation(program, "uMatrix")
        textureHandle = GLES30.glGetUniformLocation(program, "uTexture")
        texCoordHandle = GLES30.glGetAttribLocation(program, "aTexCoord")

        // Generate texture ids and load textures for each face
        GLES30.glGenTextures(6, textureIds, 0)

        val resourceId = R.drawable.photohunt_logo2
        val textureBitmap = BitmapFactory.decodeResource(context.resources, resourceId)

        if (textureBitmap == null) {
            // Handle the case where loading the texture failed
            throw RuntimeException("Failed to load texture from resource: $resourceId")
        }

        for (i in 0 until 6) {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[i])

            // Set texture parameters
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_LINEAR
            )
            GLES30.glTexParameteri(
                GLES30.GL_TEXTURE_2D,
                GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR
            )

            // Upload the texture bitmap to OpenGL ES
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, textureBitmap, 0)
        }

        // Recycle the texture bitmap since it's no longer needed
        textureBitmap.recycle()



    }


    fun draw(mvpMatrix: FloatArray) {
        // Use the program
        GLES30.glUseProgram(program)

        // Set the MVP matrix
        GLES30.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0)

        // Bind the vertex and texture coordinate buffers
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)

        val VERTEX_POS_INDX = 0
        val TEXCOORD_INDX = 1

        // Enable vertex and texture coordinate attributes
        GLES30.glEnableVertexAttribArray(VERTEX_POS_INDX)
        GLES30.glEnableVertexAttribArray(TEXCOORD_INDX)

        // Set the vertex and texture coordinate attribute pointers
        vertices.position(0)
        GLES30.glVertexAttribPointer(VERTEX_POS_INDX, 3, GLES30.GL_FLOAT, false, 0, vertices)

        texCoords.position(0)
        GLES30.glVertexAttribPointer(TEXCOORD_INDX, 2, GLES30.GL_FLOAT, false, 0, texCoords)

        // Bind the texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(
            GLES30.GL_TEXTURE_2D,
            textureIds[0]
        )

        // Set the sampler texture unit to 0
        GLES30.glUniform1i(textureHandle, 0)

        // Draw the cube
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
        indices.position(0)
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            indices.remaining(),
            GLES30.GL_UNSIGNED_SHORT,
            indices
        )

        // Disable vertex and texture coordinate attributes
        GLES30.glDisableVertexAttribArray(VERTEX_POS_INDX)
        GLES30.glDisableVertexAttribArray(TEXCOORD_INDX)
    }
}

    //fun to load shaders
private fun loadShader(type: Int, shaderCode: String): Int {
    val shader = GLES30.glCreateShader(type)
    GLES30.glShaderSource(shader, shaderCode)
    GLES30.glCompileShader(shader)

    // Check shader compilation status
    val compileStatus = IntArray(1)
    GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
    if (compileStatus[0] == 0) {
        Log.e(TAG, "Shader compilation failed for type $type:\n${GLES30.glGetShaderInfoLog(shader)}")
        GLES30.glDeleteShader(shader)
        return 0 // Return 0 to indicate compilation failure
    }

    return shader // Return the shader handle on success
}


