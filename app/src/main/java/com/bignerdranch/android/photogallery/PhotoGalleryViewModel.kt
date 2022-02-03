package com.bignerdranch.android.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

// This is our ViewModel class where we will preserve the over request of our photos over configuration change

class PhotoGalleryViewModel: ViewModel() {

    val galleryItemLiveData: LiveData<List<GalleryItem>>

            init {
                galleryItemLiveData = FlickrFetchr().fetchPhotos()
            }
}