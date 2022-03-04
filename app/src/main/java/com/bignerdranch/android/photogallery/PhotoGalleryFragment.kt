package com.bignerdranch.android.photogallery

import  android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

private const val TAG = "PhotoGalleryFragment"

// TODO - WHEN I COME BACK, AND WHEN I HAVE STRENGTH I WILL TRY TO IMPROVE THIS CHALLENGE AND THEN GO TO "WORK MANAGER."

class PhotoGalleryFragment : Fragment() {

    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>  // Our ThumbnailDownloader() Instance
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        retainInstance = true // Here we are forcefully retaining the fragment's instance because it is false by default due to re-creation in configuration change

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

        lifecycle.addObserver(thumbnailDownloader.fragmentLifeCycleObserver)
    }


    // Inflating our menu or action item(searchView)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        // getting reference to your menu_item
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView   // pulling the SearchView 'object' from our menu_item which is a searchView

        searchView.apply{

            setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                // This is called or executed when the USER submits a query in the search
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")
                    photoGalleryViewModel.fetchPhotos(queryText)

                    /**
                     *  will collapse our searchView upon the query being submitted. All the written code below are COURSE CHALLENGES.
                     **/
                    searchView.onActionViewCollapsed()

                    progressBar.visibility = View.VISIBLE
//                    photoRecyclerView.visibility = View.GONE


                    // returning true indicates that the search request has been handled
                    return false
                }

                // This is called when a text or character changes in the SearchView
                override fun onQueryTextChange(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextChange: $queryText")
                    // returning false indicates that the callback override did not handle the textChange
                    return false
                }
            })

            // This will populate the searchBox with the stored query(the USER's latest search) in the sharedPreferences when the user clicks on it and it expands
            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }
        }
    }

    // Initializing our menu_item to do work for us after it has been clicked. This is what will happen after our menu_item
    // has been clicked, which is to clear the search
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Course Challenge. Observing the fragment's view lifeCycle with the viewLifeCycleOwnerLiveData
        /**viewLifecycleOwnerLiveData.observe(
            viewLifecycleOwner, Observer { photoFragment ->
                photoFragment.lifecycle.addObserver(thumbnailDownloader.fragmentLifeCycleObserver)
            }
        )**/

        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        progressBar = view.findViewById(R.id.pBPhotos) as ProgressBar

        return view
    }

    // OUR PHOTO VIEW HOLDER AND ADAPTER

    private inner class PhotoHolder(private val itemImageView: ImageView)
        : RecyclerView.ViewHolder(itemImageView) {

            val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable


            // Use of picasso to download an image for us. Picasso comes with benefits like enhanced performance, caching and the rest.
            fun bindGalleryItem(galleryItem : GalleryItem) {
                Picasso.get()
                    .load(galleryItem.url)
                    .placeholder(R.drawable.bill_up_close)
                    .into(itemImageView)

                // Course challenge
                if (galleryItem.url.isNotEmpty()) {
                    progressBar.visibility = View.GONE
                    photoRecyclerView.visibility = View.VISIBLE
                }

                if(progressBar.visibility == View.VISIBLE) {
                    itemImageView.visibility = View.GONE
                } else {
                    itemImageView.visibility = View.VISIBLE
                }
            }
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

            holder.bindGalleryItem(galleryItem)

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
    }

    // This will remove thumbnailDownloader as a fragment's view lifecycle observer
    override fun onDestroyView() {
        super.onDestroyView()
        lifecycle.removeObserver(
            thumbnailDownloader.fragmentLifeCycleObserver)
        thumbnailDownloader.clearQueue() // Course Challenge. Have the fragment clear the queue
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