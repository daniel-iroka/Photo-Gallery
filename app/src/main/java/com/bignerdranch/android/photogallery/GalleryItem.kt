package com.bignerdranch.android.photogallery

// This is our Model class where we will store all our JSON data which includes information about a photo

data class GalleryItem(
    var title: String = "",
    var id: String = "",
    var url: String =""
)