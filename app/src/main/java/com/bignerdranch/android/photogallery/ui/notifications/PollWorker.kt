package com.bignerdranch.android.photogallery.ui.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.api.FlickrFetchr
import com.bignerdranch.android.photogallery.model.GalleryItem
import com.bignerdranch.android.photogallery.storage.QueryPreferences
import com.bignerdranch.android.photogallery.ui.NOTIFICATION_CHANNEL_ID
import com.bignerdranch.android.photogallery.ui.main.PhotoGalleryActivity

/**
 *   This is our "Worker" class where we will do the work of checking flickr for new photos and notifying the USER.
 **/

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

            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val resources = context.resources
            val notification = NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))  // the ticker will be sent to the accessibility systems in android to things like screen reader to help users with visually impairments
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // will automatically delete the notification when the user presses it
                .build()

            showBackgroundNotification(0, notification)
        }

        return Result.success()  // and this indicates the result of our operation
    }

    // This function is an ordered broadcast which will be sent to PollWorker in form of a notification invocation
    // and in turn will be sent out as as ordered broadcast instead of being posted to the NotificationManager directly
    private fun showBackgroundNotification(
        requestCode: Int,
        notification: Notification
    ) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }

        // sendOrderedBroadcast ensures that our broadcast is delivered to each receiver one at a time
        context.sendOrderedBroadcast(intent, PERM_PRIVATE)
    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION"

        const val PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }

}