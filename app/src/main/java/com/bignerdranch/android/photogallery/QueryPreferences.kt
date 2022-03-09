package com.bignerdranch.android.photogallery

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

/**
 *  This is our sharedPreferences class where we will be storing OR persist our search term over something like the device restarting.
 *  SharedPreferences is a way we store data locally in our filesystem. Readable and writable data we can access anytime using the sharedPreferences class.
 **/

private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_LAST_RESULT_ID = "lastResultId"

// This object is a singleton meaning it will only be instantiated once
object QueryPreferences {

    // function to get query stored in our sharedPreferences
    fun getStoredQuery(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    // function to store the query in our sharedPreferences
    fun setStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(PREF_SEARCH_QUERY, query)
            }
    }


    /** We are going to stash the IDs of the of the last photos in our sharedPreferences .**/

    // function to get the ID of the last photo the USER saw from the sharedPreferences
    fun getLastResultId(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_LAST_RESULT_ID, "") !!
    }

    // function to set the ID of the new photo that has been gotten from the photo request in PollWorker
    fun setLastResultId(context: Context, lastResultId: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(PREF_LAST_RESULT_ID, lastResultId)
        }
    }

}