package com.example.macc_project.photoHunt


import android.app.ActivityManager
import android.content.Intent
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.macc_project.databinding.ActivityVersusWinnerBinding
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.macc_project.utilities.ApiService
import com.example.macc_project.utilities.ExtraInfo
import com.example.macc_project.HomePageActivity
import com.example.macc_project.graphics.CubeRenderer
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

    private  var lobbyListener: ListenerRegistration? = null



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


        val db = Firebase.firestore

        val lobbyID = ExtraInfo.myLobbyID

        val lobbyRef = db.collection("lobbies").document(lobbyID)

        var player1status = ""
        var player2status = ""
        var lobbyStatus = ""


        lobbyRef
            .get()
            .addOnSuccessListener { document ->
                player1status = document.getString("player1status")!!
                player2status = document.getString("player2status")!!
                lobbyStatus = document.getString("statusGame")!!
            }

        if (player1status == "ended" && player2status == "ended" ) {
            lobbyRef.update("statusGame", "ended")
            coroutineScope.launch(Dispatchers.Main) {
                val lobbyId = ExtraInfo.myLobbyID
                getScores(lobbyId)
            }
        }

        else {
            // Listen for changes in the lobby document
            lobbyListener = lobbyRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val player1status = snapshot.getString("player1status")
                    val player2status = snapshot.getString("player2status")

                    if (player1status == "ended" && player2status == "ended") {
                        lobbyRef.update("statusGame", "ended")
                        lobbyListener?.remove()
                        coroutineScope.launch(Dispatchers.Main) {
                            val lobbyId = ExtraInfo.myLobbyID
                            getScores(lobbyId)
                        }
                    }
                }
            }
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

                    val player1NameUncut = jsonObject.get("player1").toString()
                    val player1Name = player1NameUncut.substring(1, player1NameUncut.length-1)
                    val player2NameUncut = jsonObject.get("player2").toString()
                    val player2Name = player2NameUncut.substring(1, player2NameUncut.length-1)


                    //Here set the two scores on the view
                    println("Player 1 Score: $player1Name : $player1Score")
                    println("Player 2 Score: $player2Name : $player2Score")
                    println(player1Name + player2Name)
                    if (player1Name == ExtraInfo.myUsername) {
                        binding.scoreYou.text = "${player1Score.toString()} pts"
                        binding.scoreOpp.text = "${player2Score.toString()} pts"
                    }
                    else {
                        binding.scoreYou.text = "${player2Score.toString()} pts"
                        binding.scoreOpp.text = "${player1Score.toString()} pts"
                    }

                    binding.waiting.visibility = View.GONE
                    binding.scoreTitleYou.visibility = View.VISIBLE
                    binding.scoreTitleOpp.visibility = View.VISIBLE
                    binding.scoreYou.visibility = View.VISIBLE
                    binding.scoreOpp.visibility = View.VISIBLE
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