package com.bignerdranch.android.photogallery.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val API_KEY = "332320506da66824c64330ccee5a9310"

class PhotoInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()  // We add this here to access the original request.

            val newUrl: HttpUrl = originalRequest.url().newBuilder()  // originalRequest.url() also pulls the original URL from the request
            .addQueryParameter("api_key", API_KEY)       // ....newBuilder() also adds the query parameters to it.
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras", "url_s")
            .addQueryParameter("safesearch", "1")
            .build()

        // Now we create a new request based on the original request and overwrite the url with the new URL
        val newRequest: Request = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }


}