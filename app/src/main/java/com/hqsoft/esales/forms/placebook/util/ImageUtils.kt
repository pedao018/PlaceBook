package com.hqsoft.esales.forms.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

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

    @Throws(IOException::class)
    fun createUniqueImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val filename = "PlaceBook_" + timeStamp + "_"
        val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(filename, ".jpg", filesDir)
    }

    fun decodeFileToSize(filePath: String, width: Int, height: Int): Bitmap {
        // 1
        /*
        The size of the image is loaded using BitmapFactory.decodeFile().
        The inJustDecodeBounds setting tells BitmapFactory to not load the actual image, just its size.
         */
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)

        // 2
        /*
        calculateInSampleSize() is called with the image width and height and the requested width and height.
        options is updated with the resulting inSampleSize.
        * */
        options.inSampleSize = calculateInSampleSize(
            options.outWidth, options.outHeight, width, height
        )

        // 3
        /*
        inJustDecodeBounds is set to false to load the full image this time.
        * */
        options.inJustDecodeBounds = false

        // 4
        /*
        BitmapFactory.decodeFile() loads the downsampled image from the file returns it
        * */
        return BitmapFactory.decodeFile(filePath, options)
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /*
    check the File meta data to check it’s orientation.
    This method gets the orientation in the Exif tags of a JPEG file and calls rotateImage if it is not already 0 degrees.
    * */
    @Throws(IOException::class)
    fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        val input: InputStream? = context.contentResolver.openInputStream(selectedImage)
        val path = selectedImage.path
        val ei: ExifInterface = when {
            Build.VERSION.SDK_INT > 23 && input != null -> ExifInterface(input)
            path != null -> ExifInterface(path)
            else -> null
        } ?: return img
        return when (ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90.0f) ?: img
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180.0f) ?: img
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270.0f) ?: img
            else -> img
        }
    }

    /*
    Many times when a user takes a photo in portrait mode, the image will come in rotated.
    To fix that create a rotation method that will rotate the bitmap to look correct if rotated incorrectly.
    This method will create a new bitmap and rotate it by the given degrees.
    * */
    private fun rotateImage(img: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    /*
    This uses the same technique as decodeFileToSize() to read in the size of the image first, calculate the sample size and then load in the downsampled image.
     The main difference is that it reads from the Uri stream instead of a file.
    * */
    fun decodeUriStreamToSize(
        uri: Uri,
        width: Int,
        height: Int,
        context: Context
    ): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val options: BitmapFactory.Options
            // 1 inputStream is opened for the Uri
            inputStream = context.contentResolver.openInputStream(uri)
            // 2 If the inputStream is not null, then processing continues.
            if (inputStream != null) {
                // 3 The image size is determined
                options = BitmapFactory.Options()
                options.inJustDecodeBounds = false
                BitmapFactory.decodeStream(inputStream, null, options)
                // 4 The input stream is closed and opened again, and checked for null.
                inputStream.close()
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    // 5 The image is loaded from the stream using the downsampling options and is returned to the caller.
                    options.inSampleSize = calculateInSampleSize(
                        options.outWidth, options.outHeight,
                        width, height
                    )
                    options.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeStream(
                        inputStream, null, options
                    )
                    inputStream.close()
                    return bitmap
                }
            }
            return null
        } catch (e: Exception) {
            return null
        } finally {
            // 6 You must close the inputStream once it’s opened, even if an exception is thrown.
            inputStream?.close()
        }
    }


}
