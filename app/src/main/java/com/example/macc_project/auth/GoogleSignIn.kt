package com.example.macc_project.auth

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.macc_project.utilities.ExtraInfo
import com.example.macc_project.HomePageActivity
import com.example.macc_project.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class GoogleSignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       val  gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        auth = Firebase.auth

        signIn()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign is failed", e)
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "Login with google success")
                    val userFirebase = auth.currentUser
                    if(userFirebase != null){
                        addUserToDB(userFirebase)
                    }



                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "Login with Google failed ", task.exception)
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    private fun goToHomepage(){
        val intent = Intent(this, HomePageActivity::class.java)
        startActivity(intent)

    }
    private fun addUserToDB(currentUser: FirebaseUser){
        val db = Firebase.firestore

        val user = hashMapOf(
            "email" to currentUser.email,
            "username" to currentUser.displayName!!.replace(" ", ""),
            "points" to 0
        )

        db.collection("users").document(currentUser.uid)
            .set(user)
            .addOnSuccessListener {
                ExtraInfo.setEmail(currentUser.email!!)
                ExtraInfo.setUsername(currentUser.displayName!!.replace(" ", ""))
                goToHomepage()

                Toast.makeText(
                    baseContext, "User logged with google",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error during the registration", e) }
    }


    companion object {
        const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9002
    }
}
