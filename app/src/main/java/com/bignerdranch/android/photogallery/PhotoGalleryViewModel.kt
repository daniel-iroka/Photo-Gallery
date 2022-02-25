package com.bignerdranch.android.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

/**
 *  This is our ViewModel class where we will preserve the over request of our photos over configuration change.
 * **/

class PhotoGalleryViewModel: ViewModel() {

    private val cancelRequestRepositoryClass = CancelRequestRepositoryClass()

    // This function is called when a ViewModel is about to be destroyed
    override fun onCleared() {
        super.onCleared()
        cancelRequestRepositoryClass.cancelRequestInFlight()
    }

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()

    init {
        mutableSearchTerm.value = "planets"

        // What we added here will make the ImageResults of our GalleryItem to reflect the latest request or changes of our photoSearch
        galleryItemLiveData =
            Transformations.switchMap(mutableSearchTerm) { searchItem ->
                flickrFetchr.searchPhotos(searchItem)
            }
    }

    fun fetchPhotos(query: String = "") {
        mutableSearchTerm.value = query
    }

}