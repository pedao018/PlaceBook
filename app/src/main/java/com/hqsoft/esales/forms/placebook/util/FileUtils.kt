package com.hqsoft.esales.forms.placebook.util

import android.content.Context
import java.io.File

object FileUtils {

    /*
    This is a utility method that deletes a single file in the app’s main files directory.
    You’ll use this to delete the image associated with a deleted bookmark.
    * */
    fun deleteFile(context: Context, filename: String) {
        val dir = context.filesDir
        val file = File(dir, filename)
        file.delete()
    }
}