package com.bignerdranch.android.photogallery

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 *   This is our "Worker" class where we will do the work of checking flickr for new photos and notifying the USER.
 **/

private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    // This is called from a background thread and does work in the background for us
    override fun doWork(): Result {
        Log.i(TAG, "Work request triggered")
        return Result.success()  // and this indicates the result of our operation
    }

    // todo - Later check the meaning of abstract class again maybe from intelliJ


}