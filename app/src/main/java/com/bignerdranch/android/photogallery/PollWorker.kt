package com.bignerdranch.android.photogallery

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 *   This is our "Worker" class where we will do the work of checking flickr for new photos and notifying the USER.
 **/
// TODO - WHEN I COME BACK, I WILL START REVISING FROM CHECKING NEW PHOTOS AND HALF OF NOTIFYING THE USER.

private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    // This is called from a background thread and does work in the background for us
    override fun doWork(): Result {
        val query = QueryPreferences.getStoredQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetchr().fetchPhotosRequest()  // if there is no search query(list of queries.isEmpty()), then fetch the regular interesting photos of the day.
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } else {
            FlickrFetchr().searchPhotosRequest(query)  // if there is a search query(list of queries.notEmpty()), fetch the photos, performing a search Request.
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } ?: emptyList()


        if (items.isEmpty()) {
            return Result.success()
        }

        // compares the first returned item(photos) with the last seen item(photos)
        val resultId = items.first().id
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got a new result: $resultId")
            // if new id found, pass to lastResultId
            QueryPreferences.setLastResultId(context, resultId)
        }

        return Result.success()  // and this indicates the result of our operation
    }


}