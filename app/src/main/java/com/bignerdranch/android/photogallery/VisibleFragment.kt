package com.bignerdranch.android.photogallery

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.fragment.app.Fragment

/** This is our SECOND Broadcast receiver that will do the job of intercepting intents sent to the FIRST receiver disallowing it from posting Notifications when the app is running.
 *  This type of Broadcast receiver is called a Dynamic Receiver.
 **/


private const val TAG = "VisibleFragment"

abstract class VisibleFragment : Fragment() {

    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // If we receive this, we're visible, so cancel
            // the notification
            Log.i(TAG, "canceling notification")
            resultCode = Activity.RESULT_CANCELED  // cancels the notification to be sent
        }
    }

    // This function is to register our Broadcast receiver and it is ideal to add this here because this type of receiver is tied
    // to the lifecycle of our Activity or Fragment.
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)  // code format of an intent-filter
        requireActivity().registerReceiver(
            onShowNotification,
            filter,
            PollWorker.PERM_PRIVATE,
            null
        )
    }

    // This function is to unregister our Broadcast receiver
    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }
}