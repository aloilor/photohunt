package com.example.macc_project

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import com.google.android.gms.location.LocationRequest
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.macc_project.databinding.ActivityHunt1Binding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Hunt1Activity : AppCompatActivity(), ExtraInfo.TimerUpdateListener {
    private val REQUEST_IMAGE_CAPTURE = 1

    val PERMISSIONS_ALL = arrayOf<String>(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val objectList = listOf("chair",
        "bottle",
        "cellular",
        "television",
        "key",
        "wallet")
    lateinit var objectToFind: String

    private val mExtraInfo: ExtraInfo = ExtraInfo()

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var listener: ListenerRegistration?= null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private lateinit var mLocationRequest: LocationRequest
    private val interval: Long = 3000 // 10seconds
    private val fastestInterval: Long = 5000 // 5 seconds
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLastLocation : Location


    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityHunt1Binding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHunt1Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)



        getPermissions(PERMISSIONS_ALL)

        objectToFind = objectList[(0..5).random()]
        binding.objectText.setText(objectToFind.toString())

        cameraExecutor = Executors.newSingleThreadExecutor()

        openCamera()

        getLastLocation()

        mExtraInfo.setTimerUpdateListener(this)
        mExtraInfo.startTimer()

        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        outputDirectory = getOutputDirectory()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations){
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.w("lat+long,update:","Latitude: $latitude" )
                    Log.w("lat+long,update:","Longitude: $longitude" )
                    binding.latitudeText.text = "Lat: $latitude"
                    binding.longitudeText.text = "Long: $longitude"
                }
            }
        }

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.64:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        storage = FirebaseStorage.getInstance()



    }

    override fun onTimerUpdate(minutes: Int, seconds: Int, deciseconds: Int, milliseconds: Int) {
        //println("Timer Update, $milliseconds")
        binding.timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onTimerFinished(minutes: Int, seconds: Int, deciseconds: Int, milliseconds: Int) {
        val finalTime = (seconds*1000+milliseconds).toString()
        println("finaltime: $finalTime")
        ExtraInfo.setTime(finalTime)
    }




    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER

        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {

        if (isLocationEnabled()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mLocationRequest = LocationRequest.Builder(interval).setIntervalMillis(interval).setMinUpdateIntervalMillis(fastestInterval).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                mLastLocation = task.result
                if (mLastLocation == null) {

                } else {
                    latitude = mLastLocation.latitude
                    longitude = mLastLocation.longitude

                    binding.latitudeText.text = "Latitude: $latitude"
                    binding.longitudeText.text = "Longitude: $longitude"
                }
                startLocationUpdates()
            }
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.getMainLooper()
        )
    }

    private fun getPermissions(permissions: Array<String>) {

        permissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it)
                != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(it), 1)
        }
    }

    // Handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now proceed with camera or storage operations
            } else getPermissions(PERMISSIONS_ALL)
        }
    }


    private fun openCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture = ImageCapture.Builder().build()
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mExtraInfo.stopTimer()
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image

        val fileName = "image_${System.currentTimeMillis()}.jpg"

        val photoFile = File(
            outputDirectory,
            fileName
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val imageBitmap: Bitmap  = BitmapFactory.decodeFile(photoFile.path)
                    // Convert the Bitmap to a byte array
                    val data = convertBitmapToByteArray(imageBitmap)

                    //Upload the image to the python server
                    uploadImageToServer(data)


                    // upload image to firebase storage
                    val storageRef = storage.reference.child("images").child(fileName)
                    val uploadTask = storageRef.putBytes(data)


                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
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