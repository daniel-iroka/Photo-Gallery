package com.bignerdranch.android.photogallery

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
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

        retainInstance = true // In simple terms, this is like forcefully retaining the fragment instance

        photoGalleryViewModel =     // An instance of our viewModel class
            ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)

        thumbnailDownloader = ThumbnailDownloader()
        lifecycle.addObserver(thumbnailDownloader)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, COLUMN_WIDTH)

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
        photoRecyclerView.apply{
            viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        COLUMN_WIDTH = width
                        Log.i(TAG, "Current width is $COLUMN_WIDTH. Received width is $width")
                        TODO("Check the logs for my method to see the width returned in my method")
                    }
                }
            )
        }

        // NNAMDI'S METHOD
        // Getting to know the width of our recyclerView to dynamically adjust the number of columns
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3).also {
            it.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    Log.i(TAG, "The current width is $position.")
                    return if (position % 3 == 0) 2 else 1
                    TODO("Look up Nnamdi's Implementation and check what spanSize is and why it changes anyhow.")
                }
            }
        }
    }


    // This will remove thumbnailDownloader as a lifecycle observer when onDestroy is called.
    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            thumbnailDownloader
        )
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}