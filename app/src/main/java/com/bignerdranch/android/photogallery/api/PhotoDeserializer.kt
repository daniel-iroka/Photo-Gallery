package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.model.PhotoResponse
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

// OUR CUSTOM JSON DESERIALIZER FILE
class PhotoDeserializer: JsonDeserializer<PhotoResponse> {

    lateinit var photos : PhotoResponse

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {
        val photoJsonObject = json?.asJsonObject
        val gson = Gson()

        return gson.fromJson(photoJsonObject, PhotoResponse::class.java)
    }

}