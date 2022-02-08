package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.PhotoDeserializer
import retrofit2.Call
import retrofit2.http.GET

// THIS FILE is our API-specific code interface that wil hold annotated objects that will be Implemented my Retrofit
// It will basically handle the Web Requests

interface FlickrApi {


    @GET("services/rest/?method=flickr.interestingness.getList" +
            "&api_key=332320506da66824c64330ccee5a9310" +
            "&format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s"
    )
    fun fetchPhotos(): Call<PhotoDeserializer>
}