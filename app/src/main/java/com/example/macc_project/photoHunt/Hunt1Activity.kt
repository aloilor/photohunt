package com.example.macc_project.photoHunt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import com.google.android.gms.location.LocationRequest
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.macc_project.utilities.ApiService
import com.example.macc_project.utilities.ExtraInfo
import com.example.macc_project.HomePageActivity
import com.example.macc_project.R

import com.example.macc_project.databinding.ActivityHunt1Binding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext


class Hunt1Activity : AppCompatActivity(), ExtraInfo.TimerUpdateListener, CoroutineScope {
    private val REQUEST_IMAGE_CAPTURE = 1

    val PERMISSIONS_ALL = arrayOf<String>(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val objectList = listOf(
        "desk",
        "mouse",
        "keyboard",
        "monitor",
        "laptop"
    )

    lateinit var objectToFind: String

    private val mExtraInfo: ExtraInfo = ExtraInfo()

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var listener: ListenerRegistration? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private lateinit var mLocationRequest: LocationRequest
    private val interval: Long = 5000 // 10seconds
    private val fastestInterval: Long = 3000 // 5 seconds
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var mLastLocation: Location


    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityHunt1Binding
    private lateinit var apiService: ApiService
    private val db = FirebaseFirestore.getInstance()

    private lateinit var nextLevelIntent: Intent
    private lateinit var homePageIntent: Intent

    private lateinit var job: Job

    private var hostname = "https://photohunt.loca.lt"

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHunt1Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        job = Job()

        getPermissions(PERMISSIONS_ALL)

        objectToFind = objectList[(0..4).random()]

        cameraExecutor = Executors.newSingleThreadExecutor()

        openCamera()

        getLastLocation()

        mExtraInfo.setTimerUpdateListener(this)
        mExtraInfo.startTimer()
        binding.levelText.text = "Level ${ExtraInfo.myLevel}"
        binding.scoreText.text = "Score: ${ExtraInfo.myScore}"

        binding.cameraCaptureButton.setOnClickListener {
            binding.cameraCaptureButton.isEnabled = false
            binding.cameraCaptureButton.isClickable = false
            mExtraInfo.stopTimer()
            takePhoto()
        }

        outputDirectory = getOutputDirectory()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.w("lat+long,update:","Latitude: $latitude" )
                    Log.w("lat+long,update:","Longitude: $longitude" )
                    binding.latitudeText.text = String.format("Lat: %.2f", latitude)
                    binding.longitudeText.text = String.format("Long: %.2f", longitude)
                }
            }
        }

        // Initialize Retrofit

        val okHttpClient = OkHttpClient.Builder().connectTimeout(40, TimeUnit.SECONDS).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(hostname)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        apiService = retrofit.create(ApiService::class.java)

        storage = FirebaseStorage.getInstance()

        FirebaseApp.initializeApp(this)

        binding.hintButton.setOnClickListener {
            launch {
                sendHintRequest(objectToFind)
            }
        }
    }

    override fun onTimerUpdate(minutes: Int, seconds: Int, deciseconds: Int, milliseconds: Int) {
        //println("Timer Update, $milliseconds")
        binding.timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onTimerFinished(minutes: Int, seconds: Int, deciseconds: Int, milliseconds: Int) {
        val finalTime = (seconds * 1000 + milliseconds)
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
            mLocationRequest = LocationRequest.Builder(interval).setIntervalMillis(interval)
                .setMinUpdateIntervalMillis(fastestInterval)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
            mFusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnCompleteListener(this) { task ->
                    mLastLocation = task.result
                    if (mLastLocation != null) {
                        latitude = mLastLocation.latitude
                        longitude = mLastLocation.longitude
                        var latString = String.format("%.2f", latitude)
                        var longString = String.format("%.2f", longitude)
                        println("Lat: $latString + Long: $longString")


                        binding.latitudeText.text = "Lat: $latString"
                        binding.longitudeText.text = "Long: $longString"
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
                != PackageManager.PERMISSION_GRANTED
            )
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

    fun cleanup() {
        job.cancel()
        cameraExecutor.shutdown()
        mExtraInfo.stopTimer()
    }

    override fun onDestroy() {
        cleanup()
        super.onDestroy()
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
                    val imageBitmap: Bitmap = BitmapFactory.decodeFile(photoFile.path)
                    // Convert the Bitmap to a byte array
                    val imageresized = Bitmap.createScaledBitmap(imageBitmap, 224, 224, true)
                    val data = convertBitmapToByteArray(imageresized)

                    //Upload the image to the python server
                    launch {
                        uploadImageToServer(data)
                    }


                    // upload image to firebase storage
                    val storageRef = storage.reference.child("images").child(fileName)
                    val uploadTask = storageRef.putBytes(data)


                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private suspend fun uploadImageToServer(data: ByteArray) {
        val mediaType = "image/jpeg".toMediaTypeOrNull()
        val requestFile = RequestBody.create(mediaType, data)

        var username = ExtraInfo.myUsername

        val body =
            MultipartBody.Part.createFormData("file", "$username-$objectToFind.jpg", requestFile)

        // response alert dialog
        val customLayout2: View = layoutInflater.inflate(R.layout.activity_dialog, null)

        val nextButton = customLayout2.findViewById<Button>(R.id.nextButton)
        val textResponse = customLayout2.findViewById<TextView>(R.id.textResponse)
        val messageDialog = AlertDialog.Builder(this).setView(customLayout2)

        val dialog = messageDialog.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        // button for dismiss alert dialog
        val homePageIntent = Intent(this, HomePageActivity::class.java)
        nextButton.text = "Next lvl"
        nextButton.setOnClickListener {
            dialog.dismiss()
            Intent(this, Hunt1Activity::class.java).also {
                startActivity(it)
            }
        }

        println("We are here")


        try {
            val response = withContext(Dispatchers.IO) {
                apiService.uploadImage(body).execute()
            }

            var score = 0

            if (response.code() == 200) {
                println("Object Found!")
                println("setting the score: ${ExtraInfo.actualMilliseconds}")
                if (ExtraInfo.actualMilliseconds <= ExtraInfo.scoreThreshold1ms) {
                    ExtraInfo.setScore(ExtraInfo.scoreThreshold1pts)
                    score = ExtraInfo.scoreThreshold1pts
                } else if (ExtraInfo.actualMilliseconds > ExtraInfo.scoreThreshold1ms && ExtraInfo.actualMilliseconds <= ExtraInfo.scoreThreshold2ms) {
                    ExtraInfo.setScore(ExtraInfo.scoreThreshold2pts)
                    score = ExtraInfo.scoreThreshold2pts
                } else {
                    ExtraInfo.setScore(ExtraInfo.scoreThreshold3pts)
                    score = ExtraInfo.scoreThreshold3pts
                }

                if (ExtraInfo.myLevel == ExtraInfo.MAX_LEVEL) {
                    textResponse.text =
                        "Good job, you found the right object! You gained $score points. The game is ending though ):"
                    nextButton.text = "End Page"
                    nextButton.setOnClickListener {
                        cleanup()
                        dialog.dismiss()
                        goToWinnerPage()
                    }
                } else {
                    textResponse.text =
                        "Good job, you found the right object! You gained $score points, now get to the next level, champ ;)"
                }

            } else if (response.code() == 250) {
                println("Wrong object!")
                println("setting the score: ${ExtraInfo.actualMilliseconds}")

                if (ExtraInfo.myLevel == ExtraInfo.MAX_LEVEL) {
                    textResponse.text =
                        "Tough luck buddy, that's not the right object and you lost 1 point (if you had any)! Also, the game is ending ):"
                    nextButton.text = "End Page"
                    nextButton.setOnClickListener {
                        cleanup()
                        dialog.dismiss()
                        goToWinnerPage()
                    }
                } else
                    textResponse.text =
                        "Tough luck buddy, that's not the right object ):. You'll get it next time, but in the meantime you lost 1 point (if you had any). "
                ExtraInfo.setScore(-1)
            } else {
                textResponse.text =
                    "Your image hasn't been uploaded, something's wrong with the server ): "
                nextButton.text = "Home page"
                nextButton.setOnClickListener {
                    cleanup()
                    dialog.dismiss()
                    startActivity(homePageIntent)
                }
            }
            binding.scoreText.text = "Score: ${ExtraInfo.myScore}"
            dialog.show()
            ExtraInfo.updateLevel()
            addScoreDB()
        } catch (t: Throwable) {
            textResponse.text =
                "Your image hasn't been uploaded, something's wrong with the server ): "
            nextButton.text = "Home page"
            nextButton.setOnClickListener {
                dialog.dismiss()
                startActivity(homePageIntent)
            }
            dialog.show()
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }


    private suspend fun sendHintRequest(objectToFind: String) {
        try {
            val querySnapshot = withContext(Dispatchers.IO) {
                // Perform the Firestore query on the IO thread
                db.collection("hints")
                    .whereEqualTo("request", objectToFind)
                    .get()
                    .await()
            }

            if (!querySnapshot.isEmpty) {
                val hintDoc = querySnapshot.documents[0]
                val hintId = hintDoc.id
                getHintResponse(hintId, this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error collection hints: $e")
        }
    }

    private suspend fun getHintResponse(hintID: String, activity: Activity) {
        try {
            val db = FirebaseFirestore.getInstance()
            val docHintRef = db.collection("hints").document(hintID)

            val snapshot = withContext(Dispatchers.IO) {
                docHintRef.get().await()
            }

            if (snapshot != null && snapshot.exists()) {
                val response = snapshot.getString("hint") ?: ""
                val secResponse = snapshot.getString("secondHint") ?: ""
                if (response.isNotEmpty() && secResponse.isNotEmpty()) {
                    showHintDialog(activity, response, secResponse)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error response collection hints: $e")
        }
    }

    private fun showHintDialog(activity: Activity, response: String, secResponse: String) {
        val layoutInflater = LayoutInflater.from(activity)
        val customLayout: View = layoutInflater.inflate(R.layout.activity_hint, null)
        val textResponse = customLayout.findViewById<TextView>(R.id.textResponse)
        val secHintButton = customLayout.findViewById<Button>(R.id.newButton)
        val dismissButton = customLayout.findViewById<Button>(R.id.dismissButton)

        textResponse.text = response

        val messageDialog = AlertDialog.Builder(activity)
            .setView(customLayout)

        val dialog = messageDialog.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        secHintButton.setOnClickListener {
            textResponse.text = secResponse
        }

        dismissButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun addScoreDB() {
        val db = Firebase.firestore
        println(ExtraInfo.myEmail)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef = db.collection("users").document(userId)
        userRef.update("points", ExtraInfo.myScore)
            .addOnSuccessListener {
                println("score successfully updated")

            }
            .addOnFailureListener { println("Error updating the score") }

    }
    private fun goToWinnerPage(){

        val intent = Intent(this, WinnerActivity::class.java)
        startActivity(intent)
    }
}


