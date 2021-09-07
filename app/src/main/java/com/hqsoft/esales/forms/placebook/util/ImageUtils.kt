package com.hqsoft.esales.forms.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

// 1
object ImageUtils {
    // 2
    fun saveBitmapToFile(
        context: Context, bitmap: Bitmap,
        filename: String
    ) {
        // 3
        val stream = ByteArrayOutputStream()
        // 4
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        // 5
        val bytes = stream.toByteArray()
        // 6
        saveBytesToFile(context, bytes, filename)
    }

    // 7
    private fun saveBytesToFile(
        context: Context, bytes:
        ByteArray, filename: String
    ) {
        val outputStream: FileOutputStream
        // 8
        try {
            // 9
            outputStream = context.openFileOutput(
                filename,
                Context.MODE_PRIVATE
            )
            // 10
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
        val filePath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }

}
