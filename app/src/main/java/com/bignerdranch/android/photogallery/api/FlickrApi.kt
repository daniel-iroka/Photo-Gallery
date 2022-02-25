package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.PhotoDeserializer
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


/**
 * THIS FILE is our API-specific code interface that wil hold annotated objects that will be Implemented my Retrofit
 * It will basically handle the Web Requests
 */

interface FlickrApi {

    @GET("services/rest?method=flickr.interestingness.getList")
    fun fetchPhotos(): Call<PhotoDeserializer>


    // This function will download the pictures from the flickrApi
    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

    // This function will search photos on the flickr API
    // ("text") will be appended to the URL as the request of the search due to the help of the Query annotation
    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query: String): Call<PhotoDeserializer>
}