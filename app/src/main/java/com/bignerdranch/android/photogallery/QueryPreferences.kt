package com.bignerdranch.android.photogallery

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

/**
 *  This is our sharedPreferences class where we will be storing OR persist our search term over something like the device restarting.
 *  SharedPreferences is a way we store data locally in our filesystem. Readable and writable data we can access anytime using the sharedPreferences class.
 **/

private const val PREF_SEARCH_QUERY = "searchQuery"

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
}