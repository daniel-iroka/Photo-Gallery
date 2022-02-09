package com.bignerdranch.android.photogallery

import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

// THE SOLE PURPOSE OF THIS FILE IS TO DOWNLOAD AND SERVE IMAGES TO "PhotoGalleryFragment"

private const val TAG = "ThumbnailDownloader"

class ThumbnailDownloader<in T>
    :HandlerThread(TAG), LifecycleObserver {

    private var hasQuit = false

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun setup() {
        Log.i(TAG, "Starting background thread.")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
        Log.i(TAG, "Destroying background thread.")
    }

    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a URL : $url")
    }

}