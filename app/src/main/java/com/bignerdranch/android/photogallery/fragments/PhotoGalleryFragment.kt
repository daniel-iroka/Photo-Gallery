package com.bignerdranch.android.photogallery.fragments

import  android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bignerdranch.android.photogallery.*
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.model.GalleryItem
import com.bignerdranch.android.photogallery.requests.ThumbnailDownloader
import com.bignerdranch.android.photogallery.storage.QueryPreferences
import com.bignerdranch.android.photogallery.ui.PhotoPageActivity
import com.bignerdranch.android.photogallery.ui.notifications.PollWorker
import com.bignerdranch.android.photogallery.viewmodel.PhotoGalleryViewModel
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : VisibleFragment() {

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
        val searchItem :MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView   // pulling the SearchView 'object' from our menu_item which is a searchView
        searchView.isSubmitButtonEnabled = true
        
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

                    photoRecyclerView.alpha = 0f
                    progressBar.visibility = View.VISIBLE


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


        // What we are doing here is that we are toggling the menu_item title based on the worker 'polling' state
        // since its default title is "start_polling", it will stop polling if the polling state is true so that the user can have control
        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    // Initializing our menu_item to do work for us after it has been clicked. This is what will happen after our menu_item
    // has been clicked, which is to clear the search
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }


            // this menu_item is for the toggling the worker class
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(), false)

                // this else block where we reschedule our worker to perform its work if its currently not running
                } else {
                    //Constraints are like adding extra information to our Work request and in this constraint, we request that
                    // the work should only happen in an unmetered network(Network type) to avoid unnecessary data usages.
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicRequest)
                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
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
        : RecyclerView.ViewHolder(itemImageView), View.OnClickListener {

            private lateinit var galleryItem: GalleryItem

            init {
                // after implementing View.OnclickListener, will listen to presses on an item
                itemView.setOnClickListener(this)
            }

            val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

            fun bindGalleryItem(item: GalleryItem) {
                galleryItem = item
            }

            override fun onClick(view: View) {
                // This intent is for starting off a new activity displaying the web contents of a photo's page url when the photo is clicked by the user
                val intent = PhotoPageActivity
                    .newIntent(requireContext(), galleryItem.photoPageUri)
                startActivity(intent)

                // This intent is for the page's url
                // val intent = Intent(Intent.ACTION_VIEW, galleryItem.photoPageUri)
            }

            // Use of picasso to download an image for us. Picasso comes with benefits like enhanced performance, caching and the rest.
            fun bindGalleryItems(galleryItem : GalleryItem) {
                Picasso.get()
                    .load(galleryItem.url)
                    .placeholder(R.drawable.bill_up_close)
                    .into(itemImageView)

                // Course challenge
                if (galleryItem.url.isNotEmpty()) {
                    progressBar.visibility = View.GONE
                    photoRecyclerView.alpha = 1f  // will toggle the visibility of the recylerView
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
            holder.bindGalleryItems(galleryItem)

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