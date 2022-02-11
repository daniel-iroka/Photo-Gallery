package com.bignerdranch.android.photogallery

import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

// THE SOLE PURPOSE OF THIS FILE IS TO DOWNLOAD AND SERVE IMAGES TO "PhotoGalleryFragment" in a background thread
// TODO - When I come back, I Will start revising from "Starting and stopping a HandlerThread", go to Messages and Message Handlers and beyond.

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
        start()  // will start this function when lifecycleOwner.create() starts
        looper
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
        Log.i(TAG, "Destroying background thread.")
        quit()  // this will terminate the thread
    }

    // This function expects an object of type T to use as an Identifier and a url for the image to download
    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a URL : $url")
    }

}