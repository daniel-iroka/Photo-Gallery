package com.bignerdranch.android.photogallery.model

import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>  // stores a list of "GalleryItems" serialized as photo to map the JSON object
}