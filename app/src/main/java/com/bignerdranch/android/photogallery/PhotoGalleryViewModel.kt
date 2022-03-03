package com.bignerdranch.android.photogallery

import android.app.Application
import androidx.lifecycle.*

/**
 *  This is our ViewModel class where we will preserve the over request of our photos over configuration change.
 **/

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {

    private val cancelRequestRepositoryClass = CancelRequestRepositoryClass()

    // This function is called when a ViewModel is about to be destroyed
    override fun onCleared() {
        super.onCleared()
        cancelRequestRepositoryClass.cancelRequestInFlight()
    }

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)

        // What we added here will make the ImageResults of our GalleryItem to reflect the latest request or changes of our photoSearch
        galleryItemLiveData =
            Transformations.switchMap(mutableSearchTerm) { searchItem ->
                // will still update the user with photos even though the search_item has been cleared
                if (searchItem.isBlank()) {
                    flickrFetchr.fetchPhotos()
                } else {
                    flickrFetchr.searchPhotos(searchItem)
                }
            }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }

}