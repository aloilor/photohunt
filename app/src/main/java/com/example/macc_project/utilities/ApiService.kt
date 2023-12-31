package com.example.macc_project.utilities

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


interface ApiService {
    @Multipart
    @POST("upload")
    fun uploadImage(@Part image: MultipartBody.Part):Call <Void>

    @GET("/get_lobby/{lobby_id}")
    suspend fun getLobby(@Path("lobby_id") lobbyId: String):Response<ResponseBody>
}