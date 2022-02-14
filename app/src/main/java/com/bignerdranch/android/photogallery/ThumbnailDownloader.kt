package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

// THE SOLE PURPOSE OF THIS FILE IS TO DOWNLOAD AND SERVE IMAGES TO "PhotoGalleryFragment" in a background thread
// TODO - When I come back next time, I will start revising from USING HANDLERS and then go to PASSING HANDLERS and beyond.

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0    // This will be used to identify messages as download requests

class ThumbnailDownloader<in T>
    :HandlerThread(TAG), LifecycleObserver {

    private var hasQuit = false

    // This will store a reference to 'Handler' instance that will be responsible for queueing download requests as messages onto
    // the ThumbnailDownloader background thread
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()   // This will store the identifier object and a URL for the download requests
    private val flickrFetchr = FlickrFetchr()   // and this stores a reference to flickrFetctr instance

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

    // Here we initialize requestHandler and define what it will do when downloaded messages are pulled off the queue and
    // passed to it.
    @Suppress("UNCHECKED_CAST") // This tells lint that we are well aware of casting "msg.obj as T" without first checking if msg.obj is of type T
    @SuppressLint("HandlerLeak") // Added this because of a potential lint error(which occurs when the inner class's lifetime is longer than the outer class)
    override fun onLooperPrepared() {
        requestHandler = object: Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    // Will print the "message" in the logs
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    // This function expects an object of type T to use as an Identifier and a url for the image to download
    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a URL : $url")
        // Prepares our message and sends it to its target(Handler in this case)
         // The Handler(requestHandler) then automatically becomes in charge of processing the message that will be pulled off the message queue
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD,  target)
            .sendToTarget()

    }

    // This function handles our message request. It also checks for a url and then passes it to flickr.fetchPhoto
    // This is where the downloading happens
    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return
    }

}