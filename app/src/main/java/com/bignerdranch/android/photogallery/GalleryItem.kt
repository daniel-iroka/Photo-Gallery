package com.bignerdranch.android.photogallery

// This is our Model class where we will stash all our recieved JSON data(recent interesting photos)
// This class will hold information for any photo such as its title, id, url and the rest.

data class GalleryItem(
    var title: String = "",
    var id: String = "",
    var url: String =""
)
