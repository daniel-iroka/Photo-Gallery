package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.*
import java.util.concurrent.ConcurrentHashMap

// THE SOLE PURPOSE OF THIS FILE IS TO DOWNLOAD AND SERVE IMAGES TO "PhotoGalleryFragment" in a background thread

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0    // Download object

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
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

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown() {
                Log.i(TAG, "Destroying background thread")
                lifecycle?.removeObserver(observer)
                quit()  // will terminate the thread
            }

        }


    // Course Challenge(LifeCycleObserver)
    private val observer = fragmentLifeCycleObserver
    private val lifecycle: Lifecycle? = null


    init {
        lifecycle?.addObserver(observer)
    }


    // Course Challenge(LifeCycleObserver). Have thumbnailDownloader be automatically removed as lifecycleObserver when the fragment's lifecycle.ON_DESTROY is called.
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clearQueue() {
        Log.i(TAG, "Clearing all the requests from the Queue.")
        requestHandler.removeMessages(MESSAGE_DOWNLOAD)
        requestMap.clear()
    }


    private var hasQuit = false

    // This will store a reference to 'Handler' instance that will be responsible for queueing download requests as messages onto
    // the ThumbnailDownloader background thread
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()   // This will store the identifier object and a URL for the download requests
    private val flickrFetchr = FlickrFetchr()


    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }


    // We initialize requestHandler and define what it will do when downloaded messages are pulled off the queue and passed to it.

    @Suppress("UNCHECKED_CAST") // 1 and 2. Go back to book for reference.
    @SuppressLint("HandlerLeak")
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

        val retrievedBitmap = PhotoGalleryCache.instance.retrieveBitmapFromCache("bitmap1")


        // Course Challenge. This will check if the bitmap has already been downloaded and then use it, if not proceed to download a new one.
        if (bitmap == retrievedBitmap) {

            responseHandler.post(Runnable {
                if (requestMap[target] != url || hasQuit) {
                    return@Runnable
                }

                requestMap.remove(target)
                onThumbnailDownloaded(target, retrievedBitmap)
            })
        } else {
            val bitmapDownload = flickrFetchr.fetchPhoto(url) ?: return

            responseHandler.post(Runnable {
                if (requestMap[target] != url || hasQuit) {
                    return@Runnable
                }

                requestMap.remove(target)     // we remove the photoHolder-URL mapping from the requestMap and set the bitmap on the target PhotoHolder
                onThumbnailDownloaded(target, bitmapDownload)
            })
        }

        PhotoGalleryCache.instance.saveBitmapToCache("bitmap1", bitmap)
    }

}