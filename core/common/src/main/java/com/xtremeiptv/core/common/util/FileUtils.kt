package com.xtremeiptv.core.common.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

object FileUtils {

    fun getFileSizeString(size: Long): String {
        if (size <= 0) return "0 B"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun getMimeType(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    }

    fun getFileNameFromUrl(url: String): String {
        return url.substringAfterLast("/").substringBefore("?")
    }

    fun copyUriToFile(context: Context, uri: Uri, destination: File): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destination).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getExternalStorageDirectory(context: Context): File {
        return context.getExternalFilesDir(null) ?: context.filesDir
    }

    fun getDownloadsDirectory(context: Context): File {
        val downloadsDir = File(getExternalStorageDirectory(context), "Downloads")
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        return downloadsDir
    }

    fun getRecordingsDirectory(context: Context): File {
        val recordingsDir = File(getExternalStorageDirectory(context), "Recordings")
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }
        return recordingsDir
    }

    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
