package com.bignerdranch.android.photogallery

// This is our Repository file which we will use to store and access data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bignerdranch.android.photogallery.api.FlickrApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr  {

    private val flickrApi : FlickrApi

    init {
        val retrofit : Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")  // the baseUrl is our request endpoint
            .addConverterFactory(ScalarsConverterFactory.create()) // this expects a converter factory which creates an instance of scalarConverters that will be used by retrofit
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }


    fun fetchPhotos(): LiveData<String> {
        val responseLiveData: MutableLiveData<String> = MutableLiveData()
        val flickrRequest: Call<String> = flickrApi.fetchPhotos()

        flickrRequest.enqueue(object: Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(
                call: Call<String>,
                response: Response<String>
            ) {
                Log.d(TAG,"Response received: ${response.body()}")
                responseLiveData.value = response.body()
            }
        })
        return responseLiveData
    }
}