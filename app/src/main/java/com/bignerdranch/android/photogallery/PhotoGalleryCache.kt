package com.bignerdranch.android.photogallery

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import java.lang.Exception

// THIS IS OUR CACHE WHERE WE WILL STORE AND RETRIEVE OUR BITMAP

private const val TAG = "PhotoGalleryCache"

class PhotoGalleryCache private constructor() {

    private object HOLDER {
        val INSTANCE = PhotoGalleryCache()
    }

    companion object {
        val instance: PhotoGalleryCache by lazy { HOLDER.INSTANCE }
    }

    private val lru: LruCache<Any, Any> = LruCache(1500)

    // Saving our bitmap to our cache
    fun saveBitmapToCache(key: String, bitmap: Bitmap) {
        try {
            instance.lru.put(key, bitmap)
        } catch (e: Exception) {
            // left intentionally blank
        }
        Log.i(TAG, "Got some images for you: $bitmap")
    }

    // Retrieving our bitmap from our cache
    fun retrieveBitmapFromCache(key: String): Bitmap? {
        try {
            return instance.lru.get(key) as Bitmap?
        } catch (e: Exception) {
            // left intentionally blank
        }
        return null
    }
}