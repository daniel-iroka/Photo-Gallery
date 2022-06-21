package com.bignerdranch.android.photogallery.requests

import com.bignerdranch.android.photogallery.api.PhotoDeserializer
import retrofit2.Call

// This repository class handle canceling any photo request when our ViewModel is destroyed
class CancelRequestRepositoryClass {

    private lateinit var webRequest: Call<PhotoDeserializer>

    fun cancelRequestInFlight() {
        if (::webRequest.isInitialized) {
            webRequest.cancel() // cancels our web Request and then calls onFailure()
        }
    }
}