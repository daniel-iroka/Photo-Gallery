package com.bignerdranch.android.photogallery

import  android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "PhotoGalleryFragment"
private var COLUMN_WIDTH = 1

class PhotoGalleryFragment : Fragment() {

    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>  // Our ThumbnailDownloader() Instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true // In simple terms, this is like forcefully retaining the fragment's instance

        photoGalleryViewModel =     // An instance of our viewModel class
            ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)


        // passes a handler attached to the Looper of the main(this) thread since it is put in onCreate()
        // and handles the downloaded image updating it in the UI
        val responseHandler = Handler()
        thumbnailDownloader =
            ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
                val drawable = BitmapDrawable(resources, bitmap)
                photoHolder.bindDrawable(drawable)
            }

        // thumbnailDownloader.fragmentLifeCycleObserver is the most recent refactored Implementation
        // TODO - Remember to remove this comment later...
        lifecycle.addObserver(thumbnailDownloader.fragmentLifeCycleObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // set the viewLifeCycleObserver of thumbnailDownloader to listen to the lifecycle of the fragment's view
        viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifeCycleObserver
        )

        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        return view
    }

    // OUR PHOTO VIEW HOLDER AND ADAPTER

    private class PhotoHolder(itemImageView: ImageView)
        : RecyclerView.ViewHolder(itemImageView) {

            val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable
        }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>)
        :RecyclerView.Adapter<PhotoHolder>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PhotoHolder {
            val view = layoutInflater.inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoHolder(view)
        }

        override fun getItemCount() = galleryItems.size // returns the size of the list in the recyclerView

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            val placeHolder: Drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bill_up_close
            ) ?: ColorDrawable()
            holder.bindDrawable(placeHolder)

            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }
    }


    // We set up a liveData lifecycle observer to take note and hold of the returned liveData(galleryItems)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                photoRecyclerView.adapter = PhotoAdapter(galleryItems)
            }
        )


        // TODO - IN GENERAL, GO WITH NNAMDI'S METHOD BUT SEE IF MY METHOD STILL STAND'S A CHANCE.
        // TODO - ALSO TRY TO USE 0dp ON BOTH METHODS TO SEE IF WHICH WILL WORK.
        // TODO - IF EITHER OF BOTH DOESEN'T WORK, TRY THE QUALIFIED RESOURCE METHOD THE BOOK SUGGESTED AT FIRST.

        // DANIEL'S(MY) METHOD
        // Getting to know the width of our recyclerView to dynamically adjust the number of columns
        /**photoRecyclerView.apply{
            viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        COLUMN_WIDTH = width
                        Log.i(TAG, "Current width is $COLUMN_WIDTH. Received width is $width")
                        TODO("Check the logs for my method to see the width returned in my method")
                    }
                }
            )
        } **/

        // NNAMDI'S METHOD
        // Getting to know the width of our recyclerView to dynamically adjust the number of columns
        /**photoRecyclerView.layoutManager = GridLayoutManager(context, 3).also {
            it.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    Log.i(TAG, "The current width is $position.")
                    return if (position % 3 == 0) 2 else 1
                    TODO("Look up Nnamdi's Implementation and check what spanSize is and why it changes anyhow.")
                }
            }
        } **/
    }

    // This will remove thumbnailDownloader as a fragment's view lifecycle observer
    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifeCycleObserver)
    }

    // This will remove thumbnailDownloader as a lifecycle observer when onDestroy is called.
    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            thumbnailDownloader.fragmentLifeCycleObserver
        )
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}