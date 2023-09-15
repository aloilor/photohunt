package com.example.macc_project


import android.app.ActivityManager
import android.content.ContentValues.TAG
import android.content.Intent
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.macc_project.databinding.ActivityVersusWinnerBinding
import android.os.Bundle
import android.util.Log
import com.example.macc_project.graphicsApp.CubeRenderer
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class VersusWinnerActivity : AppCompatActivity(){

    private var glSurfaceView: GLSurfaceView? = null
    private var renderer: CubeRenderer? = null
    private lateinit var binding: ActivityVersusWinnerBinding
    private var hostname = "https://photohunt.loca.lt/"
    private lateinit var apiService: ApiService
    val coroutineScope = CoroutineScope(Dispatchers.IO)


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

        val okHttpClient = OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS).build()

        //Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl(hostname)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        apiService = retrofit.create(ApiService::class.java)


        coroutineScope.launch(Dispatchers.Main) {
            val lobbyId = ExtraInfo.myLobbyID
            println(lobbyId)
            getScores(lobbyId)

        }
        binding.backButton.setOnClickListener {
            val it = Intent(this, HomePageActivity::class.java)
            startActivity(it)
        }


    }
    suspend fun getScores(lobby_id:String){
        try {
            val response = fetchLobbyById(lobby_id)

            if (response != null && response.isSuccessful) {
                val responseBody = response.body()

                println(responseBody.toString())

                if (responseBody != null) {
                    val jsonString = responseBody.string()
                    val jsonObject = JsonParser.parseString(jsonString).asJsonObject

                    val player1Score = jsonObject.get("player1pts")?.asInt ?: 0
                    val player2Score = jsonObject.get("player2pts")?.asInt ?: 0

                    //Here set the two scores on the view
                    println("Player 1 Score: $player1Score")
                    println("Player 2 Score: $player2Score")
                } else {
                    Log.w(TAG, "Response is empty")
                }
            } else {
                Log.w(TAG, "No existing lobby")
            }
        } catch (e: Exception) {
            Log.e(TAG, "An error occurred: ${e.message}")
        }
    }


    suspend fun fetchLobbyById(lobbyId: String): Response<ResponseBody> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.getLobby(lobbyId)
            } catch (e: Exception) {
                println("errore: $e" )
                Response.error(500, ResponseBody.create(null, "An error occurred"))

            }
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

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    companion object{
        private const val TAG = "VersusWinnerActivity"
    }

}