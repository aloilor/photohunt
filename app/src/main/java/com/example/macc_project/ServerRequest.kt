package com.example.macc_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.macc_project.databinding.ActivityServerRequestBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class ServerRequest : AppCompatActivity() {

    private val url = "http://192.168.1.64:5000"
    private var postBodyString: String? = null
    private var mediaType: MediaType? = null
    private var requestBody: RequestBody? = null
    private lateinit var binding: ActivityServerRequestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerRequestBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.serverButton.setOnClickListener {
            postRequest("Ciaooo",url)
        }

        /*
        val Hunt1 = Intent(this, Hunt1Activity::class.java)
       startActivity(Hunt1)
        */

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
                    val errorMessage = "Something went wrong: ${e.message}"
                    showToast(errorMessage)

                    call.cancel()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseMessage =  response.body?.string()
                runOnUiThread {
                    try {
                        if(responseMessage != null){
                            showToast(responseMessage)
                        }else {
                            showToast("Empty Response")
                        }


                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }
    private fun showToast(toastMessage: String){
        Toast.makeText(
            this,
            toastMessage,
            Toast.LENGTH_LONG).show()

    }
}