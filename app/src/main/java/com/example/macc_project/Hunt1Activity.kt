package com.example.macc_project

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.macc_project.databinding.ActivityHunt1Binding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream


class Hunt1Activity : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 101
    private val REQUEST_IMAGE_CAPTURE = 1



    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityHunt1Binding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHunt1Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.64:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        storage = FirebaseStorage.getInstance()

        getPermissions()

        binding.btnPic.setOnClickListener {
            openCamera()
        }

    }

    private fun getPermissions() {
        // Check and request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            Log.w("camera", "permission granted")
        }


        //COARSE LOCATION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            Log.w("COARSE_LOCATION", "permission granted")
        }

        //FINE LOCATION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1 )
        } else {
            Log.w("FINE_LOCATION", "permission granted")
        }

    }

    // Handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now proceed with camera or storage operations
            } else getPermissions()
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.image.setImageBitmap(imageBitmap)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child("images").child(fileName)

            // Convert the Bitmap to a byte array
            val data = convertBitmapToByteArray(imageBitmap)
            //Upload the image to the server
            uploadImageToServer(data)

            // Upload the image to Firebase Storage
            val uploadTask = storageRef.putBytes(data)
            /*
            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        Log.d("MainActivity", "Download URL: $downloadUrl")

                        // Use the downloadUrl with Glide to load and display the image
                        Glide.with(this)
                            .load(downloadUrl)
                            .error(R.drawable.img) // Set an error image if loading fails
                            .into(binding.image)

                        Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT)
                            .show()
                        // You can save the downloadUrl or use it to display the image later
                    } } else {
                    // Image upload failed
                    val exception = task.exception
                    // Handle the exception
            */
        }

    }
    private fun uploadImageToServer(data: ByteArray){
        val mediaType = "image/jpeg".toMediaTypeOrNull()
        val requestFile = RequestBody.create(mediaType, data)
        val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)

        apiService.uploadImage(body).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {

                if (response.isSuccessful) {
                    val toastMessage = "image uploaded to the server"
                    showToast(toastMessage)

                } else {
                    val toastMessage = "Upload Image Failed"
                    showToast(toastMessage)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                val toastMessage = "Upload Image Failed"
                showToast(toastMessage)
            }
        })
    }
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }
    private fun showToast(msg:String){
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
    }
    companion object {
        const val TAG = "Hunt1Activity"
    }
}