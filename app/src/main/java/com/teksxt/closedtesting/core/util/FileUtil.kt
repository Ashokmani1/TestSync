package com.teksxt.closedtesting.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object FileUtil
{
    suspend fun compressImage(uri: Uri, context: Context): Uri?
    {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)

                // Create a temporary file for the compressed image
                val tempFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val quality = 10

                // Compress and save
                FileOutputStream(tempFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                }

                // Return the URI of the compressed file
                Uri.fromFile(tempFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null // Return null if compression fails, will use original
            }
        }
    }
}