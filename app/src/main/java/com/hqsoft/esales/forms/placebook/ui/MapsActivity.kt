package com.hqsoft.esales.forms.placebook.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.hqsoft.esales.forms.placebook.R
import com.hqsoft.esales.forms.placebook.adapter.BookmarkInfoWindowAdapter
import com.hqsoft.esales.forms.placebook.databinding.ActivityMapsBinding
import com.hqsoft.esales.forms.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //private var locationRequest: LocationRequest? = null
    private lateinit var placesClient: PlacesClient

    /*A big benefit of using the ViewModel class is that it is aware of lifecycles. In this case, by viewModels<MapsViewModel> is a lazy delegate that creates a new mapsViewModel only the first time the Activity is created.
    If a configuration change happens, such as a screen rotation, by viewModels<MapsViewModel> returns the previously created MapsViewModel.
    It is this viewModels delegate that requires the Java 8 options that were added in the build.gradle file above.
    * */
    private val mapsViewModel by viewModels<MapsViewModel>()

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
        const val EXTRA_BOOKMARK_ID =
            "com.raywenderlich.placebook.EXTRA_BOOKMARK_ID"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationClient()
        setupPlacesClient()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListeners()
        createBookmarkMarkerObserver()
        getCurrentLocation()
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener {
            displayPoi(it)
        }
        map.setOnInfoWindowClickListener { handleInfoWindowClick(it) }
    }

    private fun setupPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    //Gọi vị trí hiện tại của điện thoại
    private fun setupLocationClient() {
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
    }

    //Tự động di chuyển google map đến vị trí hiện tại của điện thoại
    private fun getCurrentLocation() {
        // 1
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 2
            requestPermissions()
        } else {
            // 3 Tracking user location
            /*if (locationRequest == null) {
                locationRequest = LocationRequest.create()
                locationRequest?.let { locationRequest ->
                    // 1/*
                    // This provides a general guide to how accurate the locations should be. The following options are allowed:
                    //
                    //PRIORITY_BALANCED_POWER_ACCURACY: Use this setting if you only need accuracy to the city block level, which is around 40-100 meters. This uses very little power and only polls for location updates every 20 seconds or so. The system is likely to only use Wi-Fi or a cell tower to determine your location.
                    //
                    //PRIORITY_HIGH_ACCURACY: Use this setting if you need the most accuracy possible, normally within 10 meters. This uses the most battery power and typically polls for locations about every 5 seconds.
                    //
                    //PRIORITY_LOW_POWER: Use this setting if you only need accuracy at the city level within 10 kilometers. This uses a minimal amount of battery power.
                    //
                    //PRIORITY_NO_POWER: You normally only use this setting if your app can live with or without location data. It will not actively request any location from the system but will return a location if another app is requesting location data.
                    //
                    //Here, you set priority to LocationRequest.PRIORITY_HIGH_ACCURACY so it’ll return the most accurate location possible. In the emulator, anything less than PRIORITY_HIGH_ACCURACY may not trigger any updates to occur.
                    // */
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                    // 2
                    /*This lets you specify the desired interval in milliseconds to return updates.
                    This is simply a hint to the system, and if other apps have requested faster updates, your app gets the updates at that rate as well.
                    Here, you set the requested update interval to 5 seconds by setting interval to 5000.*/
                    locationRequest.interval = 5000 //5 seconds

                    // 3
                    /*
                    * This sets the shortest interval in milliseconds that your app is capable of handling. Since other apps can affect the update interval, this sets a hard limit on how often you’ll receive updates.
                    * Here, you set the shortest interval to 1 second with locationRequest.fastestInterval = 1000.*/
                    locationRequest.fastestInterval = 1000 //1 seconds

                    // 4
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            getCurrentLocation()
                        }
                    }
                    // 5
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback, null
                    )
                }
            }*/

            //Set isMyLocationEnabled = true thì google map api sẽ tự động thêm 1 cái dot xanh vô
            map.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    // 4
                    val latLng = LatLng(location.latitude, location.longitude)
                    // 5
                    /*map.clear()
                    map.addMarker(
                        MarkerOptions().position(latLng)
                            .title("You are here!")
                    )*/
                    // 6
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    // 7
                    map.moveCamera(update)
                } else {
                    // 8
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    //Xin quyền (Permissions)
    private fun requestPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            requestLocationPermissions()
        } else {
            //Nếu user deny quyền thì hiển thị Toast nói lý do
            Toast.makeText(this, "Hi there! Accept it, Pleasessssssss......", Toast.LENGTH_SHORT)
                .show()
            requestLocationPermissions()
        }
    }

    private fun requestLocationPermissions() {
        //Code Xin quyền
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
                requestPermissions()
            }
        }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        // 1
        val placeId = pointOfInterest.placeId

        // 2
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        // 3
        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()

        // 4
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                // 5
                val place = response.place
                /*Toast.makeText(
                    this,
                    "${place.name}, ${place.phoneNumber}, ${place.address}",
                    Toast.LENGTH_LONG
                ).show()*/
                displayPoiGetPhotoStep(place)
            }.addOnFailureListener { exception ->
                // 6
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found: " +
                                exception.message + ", " +
                                "statusCode: " + statusCode
                    )
                }
            }
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        // 1
        val photoMetadata = place
            .getPhotoMetadatas()?.get(0)
        // 2
        if (photoMetadata == null) {
            displayPoiDisplayStep(place, null)
            return
        }
        // 3
        val photoRequest = FetchPhotoRequest
            .builder(photoMetadata)
            .setMaxWidth(
                resources.getDimensionPixelSize(
                    R.dimen.default_image_width
                )
            )
            .setMaxHeight(
                resources.getDimensionPixelSize(
                    R.dimen.default_image_height
                )
            )
            .build()
        // 4
        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { fetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
                displayPoiDisplayStep(place, bitmap)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found: " + exception.message + ", " + "statusCode: " + statusCode
                    )
                }
            }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        val iconPhoto = if (photo == null) {
            BitmapDescriptorFactory.defaultMarker()
        } else {
            BitmapDescriptorFactory.fromBitmap(photo)
        }

        val marker = map.addMarker(
            MarkerOptions()
                .position(place.latLng as LatLng)
                //.icon(iconPhoto)
                .title(place.name)
                .snippet("Address: ${place.address} \n.Phone: ${place.phoneNumber}")
        )
        marker?.tag = PlaceInfo(place, photo)
        marker?.showInfoWindow()
    }

    private fun handleInfoWindowClick(marker: Marker) {
        when (marker.tag) {
            is PlaceInfo -> {
                val placeInfo = (marker.tag as PlaceInfo)
                if (placeInfo.place != null && placeInfo.image != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(
                            placeInfo.place,
                            placeInfo.image
                        )
                    }
                }
                marker.remove();
            }
            is MapsViewModel.BookmarkMarkerView -> {
                val bookmarkMarkerView = (marker.tag as
                        MapsViewModel.BookmarkMarkerView)
                marker.hideInfoWindow()
                bookmarkMarkerView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    private fun createBookmarkMarkerObserver() {
        // 1
        mapsViewModel.getBookmarkMarkerViews()?.observe(
            this, {
                // 2
                map.clear()
                // 3
                it?.let {
                    displayAllBookmarks(it)
                }
            })
    }

    private fun displayAllBookmarks(bookmarks: List<MapsViewModel.BookmarkMarkerView>) {
        bookmarks.forEach { addPlaceMarker(it) }
    }

    private fun addPlaceMarker(bookmark: MapsViewModel.BookmarkMarkerView): Marker? {
        val marker = map.addMarker(
            MarkerOptions()
                .position(bookmark.location)
                .title(bookmark.name)
                .snippet(bookmark.phone)
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    )
                )
                .alpha(0.8f)
        )

        marker.tag = bookmark

        return marker
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    class PlaceInfo(
        val place: Place? = null,
        val image: Bitmap? = null
    )

}