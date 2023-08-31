package com.example.macc_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.macc_project.databinding.ActivityStartGameBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class StartGame : AppCompatActivity() {

    private val url = "http://10.0.2.2:5000/"
    private var postBodyString: String? = null
    private var mediaType: MediaType? = null
    private var requestBody: RequestBody? = null
    private lateinit var binding: ActivityStartGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartGameBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.serverButton.setOnClickListener {
            postRequest("Ciaooo",url)
        }

    }
    private fun buildRequestBody(msg: String): RequestBody {
        postBodyString = msg
        mediaType = "text/plain".toMediaType()
        requestBody = RequestBody.create(mediaType, postBodyString!!)
        return requestBody!!
    }
    private fun postRequest(message: String, URL: String) {
        val requestBody = buildRequestBody(message)
        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
            .post(requestBody)
            .url(URL)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this,"Something gone wrong", Toast.LENGTH_SHORT).show()

                    call.cancel()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    try {
                        Toast.makeText(
                            this,
                            response.body?.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }
}