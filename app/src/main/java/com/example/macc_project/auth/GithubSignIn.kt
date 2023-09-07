package com.example.macc_project.auth

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.macc_project.Hunt1Activity
import com.example.macc_project.LobbyGame
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GithubSignIn : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        val provider = getProvider()
        getPendingAuthResult(provider)


    }
    private fun getProvider(): OAuthProvider.Builder{
        val provider = OAuthProvider.newBuilder("github.com")
        provider.addCustomParameter("login", "your-email@gmail.com")
        provider.scopes = listOf("user:email")
        return provider
    }

    private fun getPendingAuthResult(provider: OAuthProvider.Builder){
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                .addOnSuccessListener {
                    // User is signed in.
                    // IdP data available in
                    // authResult.getAdditionalUserInfo().getProfile().
                    // The OAuth access token can also be retrieved:
                    // ((OAuthCredential)authResult.getCredential()).getAccessToken().
                    // The OAuth secret can be retrieved by calling:
                    // ((OAuthCredential)authResult.getCredential()).getSecret().

                }
                .addOnFailureListener {
                    Log.w(TAG, "Login with GitHub failed")
                }
        } else {
            auth
                .startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener {
                    Log.d(TAG, "Login with GitHub success")
                    val userFirebase = auth.currentUser
                    if(userFirebase != null){
                        addUserToDB(userFirebase)
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Login with GitHub failed")
                }
        }
    }


    /*private fun startActivityForLinkWithProvider(provider: OAuthProvider.Builder){
        // The user is already signed-in.
        val firebaseUser = auth.currentUser!!
        firebaseUser
            .startActivityForLinkWithProvider(this, provider.build())
            .addOnSuccessListener {
                // Provider credential is linked to the current user.
                // IdP data available in
                // authResult.getAdditionalUserInfo().getProfile().
                // The OAuth access token can also be retrieved:
                // authResult.getCredential().getAccessToken().
                // The OAuth secret can be retrieved by calling:
                // authResult.getCredential().getSecret().
            }
            .addOnFailureListener {
                // Handle failure.
            }
    }
    private fun startActivityForReauthenticateWithProvider(provider: OAuthProvider.Builder){
        // The user is already signed-in.
        val firebaseUser = auth.currentUser!!
        firebaseUser
            .startActivityForReauthenticateWithProvider(this, provider.build())
            .addOnSuccessListener {
                // User is re-authenticated with fresh tokens and
                // should be able to perform sensitive operations
                // like account deletion and email or password
                // update.
            }
            .addOnFailureListener {
                // Handle failure.
            }
    }*/
    private fun gotToStartGame(){
        val intent = Intent(this, LobbyGame::class.java)
        startActivity(intent)
    }
    private fun addUserToDB(currentUser: FirebaseUser){
        val db = Firebase.firestore

        val user = hashMapOf(
            "email" to currentUser.email,
        )

        db.collection("users").document(currentUser.uid)
            .set(user)
            .addOnSuccessListener {
                gotToStartGame()
                Toast.makeText(
                    baseContext, "User logged with GitHub",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error during the registration", e) }
    }

    companion object{
       const val TAG = "GithubActivity"
    }
}