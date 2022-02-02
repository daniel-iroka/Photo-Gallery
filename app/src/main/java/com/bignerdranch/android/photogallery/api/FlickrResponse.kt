package com.bignerdranch.android.photogallery.api

// This class maps to the outermost object("photos, stat") in the JSON hierarchy
// In simple terms, it maps to everything in the JSON data

class FlickrResponse {
    lateinit var photos: PhotoResponse
}