package com.bignerdranch.android.photogallery.api

import retrofit2.Call
import retrofit2.http.GET

// THIS FILE is our API-specific code interface that wil hold annotated objects that will Implemented my Retrofit
// It will basically handle the Web Requests

interface FlickrApi {

    // Todo - When I come back, i will go through "DEFINING AN API INTERFACE" again to understand it better.

    @GET("/")
    fun fetchContents() : Call<String>
}