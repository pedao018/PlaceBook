package com.hqsoft.esales.forms.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.hqsoft.esales.forms.placebook.databinding.ActivityBookmarkDetailsBinding
import com.hqsoft.esales.forms.placebook.R
import com.hqsoft.esales.forms.placebook.util.ImageUtils
import com.hqsoft.esales.forms.placebook.viewmodel.BookmarkDetailsViewModel
import java.io.File
import java.net.URLEncoder

class BookmarkDetailsActivity : AppCompatActivity(),
    PhotoOptionDialogFragment.PhotoOptionDialogListener {
    private lateinit var databinding: ActivityBookmarkDetailsBinding
    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsViewModel.BookmarkDetailsView? = null
    private var photoFile: File? = null

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = DataBindingUtil.setContentView(this, R.layout.activity_bookmark_details)
        setupToolbar()
        getIntentData()
        setupFab()
    }

    private fun setupFab() {
        databinding.fab.setOnClickListener { sharePlace() }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_save -> {
            saveChanges()
            true
        }
        R.id.action_delete -> {
            deleteBookmark()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun getIntentData() {
        // 1
        val bookmarkId = intent.getLongExtra(
            MapsActivity.EXTRA_BOOKMARK_ID, 0
        )
        // 2
        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(this, {
            // 3
            it?.let {
                bookmarkDetailsView = it
                // 4
                databinding.bookmarkDetailsView = it
                populateImageView()
                populateCategoryList()
            }
        })
    }

    private fun setupToolbar() {
        setSupportActionBar(databinding.toolbar)
    }

    private fun populateImageView() {
        bookmarkDetailsView?.let { bookmarkView ->
            val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                databinding.imageViewPlace.setImageBitmap(placeImage)
            }
            databinding.imageViewPlace.setOnClickListener {
                replaceImage()
            }
        }
    }

    override fun onCaptureClick() {
        // 1
        photoFile = null
        try {
            // 2
            photoFile = ImageUtils.createUniqueImageFile(this)
        } catch (ex: java.io.IOException) {
            // 3
            return
        }
        // 4
        photoFile?.let { photoFile ->
            // 5
            val photoUri = FileProvider.getUriForFile(
                this,
                "com.raywenderlich.placebook.fileprovider",
                photoFile
            )
            // 6
            val captureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            // 7
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
            // 8
            val intentActivities = packageManager.queryIntentActivities(
                captureIntent, PackageManager.MATCH_DEFAULT_ONLY
            )
            intentActivities.map { it.activityInfo.packageName }
                .forEach {
                    grantUriPermission(
                        it, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
            // 9
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }

    override fun onPickClick() {
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    /*
    To process the results of the image selection, you need a method that returns a downsampled Bitmap from a Uri path.
    This method uses the new decodeUriStreamToSize method to load the downsampled image and return it.
    * */
    private fun getImageWithAuthority(uri: Uri) = ImageUtils.decodeUriStreamToSize(
        uri,
        resources.getDimensionPixelSize(R.dimen.default_image_width),
        resources.getDimensionPixelSize(R.dimen.default_image_height),
        this
    )

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 1
        if (resultCode == android.app.Activity.RESULT_OK) {
            // 2
            when (requestCode) {
                // 3
                REQUEST_CAPTURE_IMAGE -> {
                    // 4
                    val photoFile = photoFile ?: return
                    // 5
                    val uri = FileProvider.getUriForFile(
                        this,
                        "com.raywenderlich.placebook.fileprovider",
                        photoFile
                    )
                    revokeUriPermission(
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    // 6
                    val image = getImageWithPath(photoFile.absolutePath)
                    val bitmap = ImageUtils.rotateImageIfRequired(this, image, uri)
                    updateImage(bitmap)
                }

                REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null) {
                    val imageUri = data.data as Uri
                    val image = getImageWithAuthority(imageUri)
                    image?.let {
                        val bitmap = ImageUtils.rotateImageIfRequired(this, it, imageUri)
                        updateImage(bitmap)
                    }
                }
            }
        }
    }

    private fun getImageWithPath(filePath: String) = ImageUtils.decodeFileToSize(
        filePath,
        resources.getDimensionPixelSize(R.dimen.default_image_width),
        resources.getDimensionPixelSize(R.dimen.default_image_height)
    )

    private fun updateImage(image: Bitmap) {
        bookmarkDetailsView?.let {
            databinding.imageViewPlace.setImageBitmap(image)
            it.setImage(this, image)
        }
    }

    private fun populateCategoryList() {
        // 1
        val bookmarkView = bookmarkDetailsView ?: return
        // 2
        val resourceId = bookmarkDetailsViewModel.getCategoryResourceId(bookmarkView.category)
        // 3
        resourceId?.let { databinding.imageViewCategory.setImageResource(it) }
        // 4
        val categories = bookmarkDetailsViewModel.getCategories()
        // 5
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // 6
        databinding.spinnerCategory.adapter = adapter
        // 7
        val placeCategory = bookmarkView.category
        databinding.spinnerCategory.setSelection(adapter.getPosition(placeCategory))

        // 1
        /*
        The need to use spinnerCategory.post is due to an unfortunate side effect in Android where onItemSelected() is always called once with an initial position of 0.
        This causes the spinner to reset back to the first category regardless of the selection you set programmatically.
        Using post causes the code block to be placed on the main thread queue, and the execution of the code inside the braces gets delayed until the next message loop.
        This eliminates the initial call by Android to onItemSelected().
        * */
        databinding.spinnerCategory.post {
            // 2
            databinding.spinnerCategory.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        // 3
                        val category = parent.getItemAtPosition(position) as String
                        val resourceId = bookmarkDetailsViewModel.getCategoryResourceId(category)
                        resourceId?.let {
                            databinding.imageViewCategory.setImageResource(it)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // NOTE: This method is required but not used.
                    }
                }
        }

    }

    private fun saveChanges() {
        val name = databinding.editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }
        bookmarkDetailsView?.let { bookmarkView ->
            bookmarkView.name = databinding.editTextName.text.toString()
            bookmarkView.notes = databinding.editTextNotes.text.toString()
            bookmarkView.address = databinding.editTextAddress.text.toString()
            bookmarkView.phone = databinding.editTextPhone.text.toString()
            bookmarkView.category = databinding.spinnerCategory.selectedItem as String
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }

    private fun deleteBookmark() {
        val bookmarkView = bookmarkDetailsView ?: return

        AlertDialog.Builder(this)
            .setMessage("Delete?")
            .setPositiveButton("Ok") { _, _ ->
                bookmarkDetailsViewModel.deleteBookmark(bookmarkView)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .create().show()
    }

    /*
    Android allows you to share data with other apps using an Intent with an ACTION_SEND action.
    All you need to do is provide the data.
    Android figures out the apps that support your data type and presents the user with a list of choices.
    * */
    private fun sharePlace() {
        // 1
        val bookmarkView = bookmarkDetailsView ?: return
        // 2
        /*
        This section of code builds out a Google Maps URL to trigger driving directions to the bookmarked place.
        Read the documentation at https://developers.google.com/maps/documentation/urls/guide for details about constructing map URLs.
        There are two different styles of URLs to use depending on whether a place ID is available.
        If the user creates an ad-hoc bookmark, then the directions go directly to the latitude/longitude of the bookmark.
        If the bookmark is created from a place, then the directions go to the place based on its ID.
        * */
        var mapUrl = ""
        if (bookmarkView.placeId == null) {
            // 3
            val location = URLEncoder.encode(
                "${bookmarkView.latitude},"
                        + "${bookmarkView.longitude}", "utf-8"
            )
            mapUrl = "https://www.google.com/maps/dir/?api=1" +
                    "&destination=$location"
        } else {
            // 4
            val name = URLEncoder.encode(bookmarkView.name, "utf-8")
            mapUrl = "https://www.google.com/maps/dir/?api=1" +
                    "&destination=$name&destination_place_id=" +
                    "${bookmarkView.placeId}"
        }
        // 5
        /*
        You create the sharing Activity Intent and set the action to ACTION_SEND.
        This tells Android that this Intent is meant to share its data with another application installed on the device.
        * */
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        // 6
        /*
        Multiple types of extra data can be added to the Intent.
        The app that receives the Intent can choose which of the data items to use and which to ignore.
        For example, an email app will use the ACTION_SUBJECT, but a messaging app will likely ignore it.
        There are several other extras available including EXTRA_EMAIL, EXTRA_CC, and EXTRA_BCC.
        * */
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Check out ${bookmarkView.name} at:\n$mapUrl"
        )
        sendIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            "Sharing ${bookmarkView.name}"
        )
        // 7
        /*
        The Intent type is set to a MIME type of “text/plain”.
        This instructs Android that you intend to share plain text data.
        Any app in the system that registers an intent filter for the “text/plain” MIME type will be offered as a choice in the share dialog.
        If you were sharing binary data such as an image, you might use a MIME type of “image/jpeg”.
        * */
        sendIntent.type = "text/plain"
        // 8
        startActivity(sendIntent)
    }


    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }
}
