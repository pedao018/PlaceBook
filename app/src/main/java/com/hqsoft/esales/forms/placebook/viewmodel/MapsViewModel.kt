package com.hqsoft.esales.forms.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.hqsoft.esales.forms.placebook.model.Bookmark
import com.hqsoft.esales.forms.placebook.repository.BookmarkRepo
import com.hqsoft.esales.forms.placebook.util.ImageUtils

// 1/*
// When creating a ViewModel, it should inherit from ViewModel or AndroidViewModel.
// Inheriting from AndroidViewModel allows you to include the application context which is needed when creating the BookmarkRepo
// */
class MapsViewModel(application: Application) :
    AndroidViewModel(application) {

    private val TAG = "MapsViewModel"
    private var bookmarks: LiveData<List<BookmarkView>>? = null


    // 2
    private val bookmarkRepo: BookmarkRepo = BookmarkRepo(
        getApplication()
    )

    // 3
    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
        // 4
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()
        bookmark.category = getPlaceCategory(place)
        // 5
        val newId = bookmarkRepo.addBookmark(bookmark)

        image?.let { bookmark.setImage(it, getApplication()) }

        Log.e(TAG, "New bookmark $newId added to the database.")
    }

    fun getBookmarkViews(): LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

    private fun mapBookmarksToBookmarkView() {
        // 1
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks) { repoBookmarks ->
            // 2
            repoBookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    private fun getPlaceCategory(place: Place): String {
        // 1
        var category = "Other"
        val types = place.types

        types?.let { placeTypes ->
            // 2
            if (placeTypes.size > 0) {
                // 3
                val placeType = placeTypes[0]
                category = bookmarkRepo.placeTypeToCategory(placeType)
            }
        }
        // 4
        return category
    }

    fun addBookmark(latLng: LatLng): Long? {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.name = "Untitled"
        bookmark.longitude = latLng.longitude
        bookmark.latitude = latLng.latitude
        bookmark.category = "Other"
        return bookmarkRepo.addBookmark(bookmark)
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark) = BookmarkView(
        bookmark.id,
        LatLng(bookmark.latitude, bookmark.longitude),
        bookmark.name,
        bookmark.phone,
        bookmarkRepo.getCategoryResourceId(bookmark.category)
    )

    data class BookmarkView(
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0),
        var name: String = "",
        var phone: String = "",
        val categoryResourceId: Int? = null
    ) {
        fun getImage(context: Context) = id?.let {
            ImageUtils.loadBitmapFromFile(context, Bookmark.generateImageFilename(it))
        }
    }


}
