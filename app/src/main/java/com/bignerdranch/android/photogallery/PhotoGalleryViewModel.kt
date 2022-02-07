package com.bignerdranch.android.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

// This is our ViewModel class where we will preserve the over request of our photos over configuration change

class PhotoGalleryViewModel: ViewModel() {

    private val cancelRequestRepositoryClass = CancelRequestRepositoryClass()

    // This function is called when a ViewModel is about to be destroyed
    override fun onCleared() {
        super.onCleared()
        cancelRequestRepositoryClass.cancelRequestInFlight()
    }

    val galleryItemLiveData: LiveData<List<GalleryItem>>

            init {
                galleryItemLiveData = FlickrFetchr().fetchPhotos()
            }
}