package com.bignerdranch.android.photogallery

// This is our Repository file which we will use to store and access data(photos)

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.PhotoResponse
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr  {

    private val flickrApi : FlickrApi
    
    init {
        val gsonPhotoDeserializer = GsonBuilder()  // Our Gson instance
            .registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer())
            .create()
        
        // custom converter factory
        val customGsonConverterFactory = GsonConverterFactory.create(gsonPhotoDeserializer)
        
        val retrofit : Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")  // the baseUrl is our request endpoint
            .addConverterFactory(customGsonConverterFactory)
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }


    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<PhotoDeserializer> = flickrApi.fetchPhotos()

        flickrRequest.enqueue(object: Callback<PhotoDeserializer> {
            override fun onFailure(call: Call<PhotoDeserializer>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(
                call: Call<PhotoDeserializer>,
                response: Response<PhotoDeserializer>
            ) {
                // This whole block of code digs the galleryItem list out of the response and updates the live data object with the list(photos).
                Log.d(TAG,"Response received $response")
                val flickrResponse: PhotoDeserializer? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItems
            }
        })
        return responseLiveData
    }

    @WorkerThread
    fun fetchPhotoUrl(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decode bitmap=$bitmap from Response=$response")
        return bitmap
    }
}
