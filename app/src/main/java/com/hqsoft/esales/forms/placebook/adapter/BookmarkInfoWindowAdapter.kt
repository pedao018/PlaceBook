package com.hqsoft.esales.forms.placebook.adapter

import android.app.Activity
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.hqsoft.esales.forms.placebook.databinding.ContentBookmarkInfoBinding
import com.hqsoft.esales.forms.placebook.ui.MapsActivity
import com.hqsoft.esales.forms.placebook.viewmodel.MapsViewModel

class BookmarkInfoWindowAdapter(val context: Activity) : GoogleMap.InfoWindowAdapter {

    // 3
    private val binding = ContentBookmarkInfoBinding.inflate(context.layoutInflater)

    // 4
    override fun getInfoWindow(marker: Marker): View? {
        // This function is required, but can return null if
        // not replacing the entire info window
        return null
    }

    // 5
    override fun getInfoContents(marker: Marker): View? {
        binding.title.text = marker.title ?: ""
        binding.phone.text = marker.snippet ?: ""
        val imageView = binding.photo
        //imageView.setImageBitmap((marker.tag as MapsActivity.PlaceInfo).image)
        when (marker.tag) {
            // 1
            is MapsActivity.PlaceInfo -> {
                imageView.setImageBitmap(
                    (marker.tag as MapsActivity.PlaceInfo).image
                )
            }
            // 2
            is MapsViewModel.BookmarkView -> {
                val bookMarkview = marker.tag as
                        MapsViewModel.BookmarkView
                imageView.setImageBitmap(bookMarkview.getImage(context))
            }
        }
        return binding.root
    }
}