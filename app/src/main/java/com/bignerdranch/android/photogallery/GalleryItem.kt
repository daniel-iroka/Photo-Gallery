package com.bignerdranch.android.photogallery

import android.net.Uri
import com.google.gson.annotations.SerializedName

// This is our Model class where we will stash all our received JSON data(recent interesting photos)
// This class will hold information for any photo such as its title, id, url and the rest.
// This class will map our JSON array("id, owner, url_s") and now owner for the page's URL

data class GalleryItem(
    var title: String = "",
    var id: String = "",
    // With this annotation here, we do not need to use the exact same name for our model objects as the JSON objects
    @SerializedName("url_s") var url: String ="",
    @SerializedName("owner") var owner: String = ""

) {

    // This is us generating the page URL for our photos
    val photoPageUri: Uri
        get() {
            return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build()
        }
}
