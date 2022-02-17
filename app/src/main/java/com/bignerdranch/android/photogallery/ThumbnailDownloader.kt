package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

// THE SOLE PURPOSE OF THIS FILE IS TO DOWNLOAD AND SERVE IMAGES TO "PhotoGalleryFragment" in a background thread

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0    // will identify messages as download requests

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit  // this will be called when an image has been downloaded and will be update the UI
)
    :HandlerThread(TAG) {

    // Our fragment's lifecycleObserver
    val fragmentLifeCycleObserver: LifecycleObserver =
        object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun setup() {
                Log.i(TAG, "Start background thread.")
                start()   // will start this function when the lifeCycleOwner.onCreate() starts
                looper
            }

            fun tearDown() {
                Log.i(TAG, "Destroying background thread")
                quit()  // will terminate the thread
            }
        }

    // A new observer that will listen to the lifecycle callbacks of the fragment's view
    val viewLifeCycleObserver: LifecycleObserver =
        object: LifecycleObserver {

            fun clearQueue() {
                Log.i(TAG, "Clearing all the requests from the Queue")
                requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
        }

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

        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit) {
                return@Runnable
            }

            requestMap.remove(target)     // we remove the photoHolder-URL mapping from the requestMap and set the bitmap on the target PhotoHolder
            onThumbnailDownloaded(target, bitmap)
        })
    }

}