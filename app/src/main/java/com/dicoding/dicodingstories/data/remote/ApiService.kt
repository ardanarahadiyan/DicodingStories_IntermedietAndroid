package com.dicoding.dicodingstories.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    fun postRegister(
        @Field("name") name : String,
        @Field("email") email :String,
        @Field("password") password : String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun postLogin(
        @Field("email") email :String,
        @Field("password") password : String
    ): Call<LoginResponse>

    @GET ("stories")
    suspend fun getAllPost(
        @Header("Authorization") token : String,
        @Query("page") page : Int ?= 1,
        @Query("size") size : Int ?= 15
    ) : Response<PostResponse>

    @GET ("stories/{id}")
    fun getSinglePost(
        @Header("Authorization") token : String,
        @Path ("id") id : String
    ): Call<SinglePostResponse>

    @Multipart
    @POST("/v1/stories")
    fun uploadImage(
        @Header("Authorization") token : String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") latitude : RequestBody?,
        @Part("lon") longitude : RequestBody?
    ): Call<NewPostResponse>

    @GET ("stories?size=20&location=1")
    fun getPostLWithLoc(
        @Header("Authorization") token : String,
    ): Call<MapResponse>

}