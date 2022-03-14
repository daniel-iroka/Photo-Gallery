package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient

private const val ARG_URI = "photo_page_url"

// TODO - WHEN I COME BACK, I WILL START REVISING FROM "Browsing the Web and WebView" AND THEN GO TO "Using WebChromeClient to spruce things up".

class PhotoPageFragment : VisibleFragment() {
    private lateinit var uri : Uri
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = arguments?.getParcelable(ARG_URI) ?: Uri.EMPTY
    }

    @SuppressLint("setJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_page, container, false)

        webView = view.findViewById(R.id.web_view)
        webView.settings.javaScriptEnabled = true  // enabling javascript in our webView
        webView.webViewClient = WebViewClient()
        webView.loadUrl(uri.toString())   /// loading the url in our webView

        return view
    }

    companion object {
        fun newInstance(uri: Uri?): PhotoPageFragment {
            return PhotoPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_URI, uri)
                }
            }
        }
    }
}