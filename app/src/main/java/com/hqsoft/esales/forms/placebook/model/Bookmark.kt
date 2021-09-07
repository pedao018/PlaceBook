package com.hqsoft.esales.forms.placebook.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hqsoft.esales.forms.placebook.util.FileUtils
import com.hqsoft.esales.forms.placebook.util.ImageUtils

// 1
@Entity
// 2
data class Bookmark(
    // 3
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    // 4
    var placeId: String? = null,
    var name: String = "",
    var address: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = "",
    var notes: String = "",
    var category: String = ""
) {
    // 1
    fun setImage(image: Bitmap, context: Context) {
        // 2
        id?.let {
            ImageUtils.saveBitmapToFile(
                context, image,
                generateImageFilename(it)
            )
        }
    }

    fun deleteImage(context: Context) {
        id?.let {
            FileUtils.deleteFile(context, generateImageFilename(it))
        }
    }

    //3
    companion object {
        fun generateImageFilename(id: Long): String {
            // 4
            return "bookmark$id.png"
        }
    }
}