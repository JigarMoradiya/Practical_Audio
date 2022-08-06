package com.jigar.audioplayer.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object FileUtil {

    fun getAudioFilePathDuration(filePath: String?): Long {
        if (filePath.isNullOrEmpty()) {
            return 0
        }
        // load data file
        val metaRetriever = MediaMetadataRetriever()
        metaRetriever.setDataSource(filePath)

        // get duration
        val duration: String? =
            metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val seconds: Long = if (!duration.isNullOrEmpty()) {
            val dur: Long = (duration.toLong())
            (dur / 1000)
        } else {
            0L
        }

        // close object
        metaRetriever.release()
        return seconds
    }

    fun getFilePathFromUri(context: Context, uri: Uri?): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val newUri = getFilePathFromUriQ(context, uri)
            newUri?.path
        } else {
            uri?.let { getPath(it, context) }
        }
    }

    @SuppressLint("NewApi")
    fun getFilePathFromUriQ(context: Context, uri: Uri?): Uri? {
        uri?.let {
            val fileName = getFileName(context, uri)
            if (fileName != null) {
                val file = File(context.externalCacheDir, fileName)
                file.createNewFile()
                FileOutputStream(file).use { outputStream ->
                    context.contentResolver.openInputStream(uri).use { inputStream ->
                        if (inputStream != null) {
                            FileUtils.copy(inputStream, outputStream)
                        }
                        outputStream.flush()
                    }
                }
                return Uri.fromFile(file)
            }
        }
        return null
    }

    @SuppressLint("Range")
    fun getFileName(context: Context, uri: Uri?): String? {
        var result: String? = null
        if (uri?.scheme == "content") {
            val cursor: Cursor? = context.contentResolver?.query(uri, null, null, null, null)
            cursor.use { cursor1 ->
                if (cursor1 != null && cursor1.moveToFirst()) {
                    result = cursor1.getString(cursor1.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri?.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut ?: (0 + 1))
            }
        }
        return result
    }


    private fun getPath(uri: Uri, context: Context): String? {
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            val fullPath = getPathFromExtSD(split)
            return if (fullPath !== "") {
                fullPath
            } else {
                null
            }
        }

        // DownloadsProvider
        if (isDownloadsDocument(uri)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                var cursor: Cursor? = null
                try {
                    cursor = context.contentResolver.query(
                        uri,
                        arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                        null,
                        null,
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val fileName: String = cursor.getString(0)
                        val path = Environment.getExternalStorageDirectory()
                            .toString() + "/Download/" + fileName
                        if (!TextUtils.isEmpty(path)) {
                            return path
                        }
                    }
                } finally {
                    cursor?.close()
                }
                val id: String = DocumentsContract.getDocumentId(uri)
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:".toRegex(), "")
                    }
                    val contentUriPrefixesToTry = arrayOf(
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads"
                    )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        return try {
                            val contentUri = ContentUris.withAppendedId(
                                Uri.parse(contentUriPrefix),
                                java.lang.Long.valueOf(id)
                            )
                            getDataColumn(context, contentUri, null, null)
                        } catch (e: NumberFormatException) {
                            //In Android 8 and Android P the id is not a number
                            uri.path!!.replaceFirst("^/document/raw:".toRegex(), "")
                                .replaceFirst("^raw:".toRegex(), "")
                        }
                    }
                }
            } else {
                val id = DocumentsContract.getDocumentId(uri)
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:".toRegex(), "")
                }
                val contentUri: Uri? = try {
                    ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    null
                }
                return getDataColumn(context, contentUri, null, null)
            }
        }


        // MediaProvider
        if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
                "image" -> {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                "video" -> {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
                "audio" -> {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            selection = "_id=?"
            selectionArgs = arrayOf(split[1])
            return getDataColumn(
                context, contentUri, selection,
                selectionArgs
            )
        }
        if (isGoogleDriveUri(uri)) {
            return getDriveFilePath(uri, context)
        }
        if (isWhatsAppFile(uri)) {
            return getFilePathForWhatsApp(uri, context)
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }
            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(uri, context)
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // return getFilePathFromURI(context,uri);
                copyFileToInternalStorage(uri, "userfiles", context)
                // return getRealPathFromURI(context,uri);
            } else {
                getDataColumn(context, uri, null, null)
            }
        }
        if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun fileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    private fun getPathFromExtSD(pathData: Array<String>): String {
        val type = pathData[0]
        val relativePath = "/" + pathData[1]
        var fullPath = ""

        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equals(type, ignoreCase = true)) {
            fullPath = Environment.getExternalStorageDirectory().toString() + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath
        if (fileExists(fullPath)) {
            return fullPath
        }
        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath
        return if (fileExists(fullPath)) {
            fullPath
        } else fullPath
    }

    private fun getDriveFilePath(uri: Uri, context: Context): String? {
        val returnCursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        if (returnCursor != null) {
            /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val size = returnCursor.getLong(sizeIndex).toString()
            val file = File(context.cacheDir, name)
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                var read = 0
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable: Int? = inputStream?.available()

                //int bufferSize = 1024;
                val bufferSize = bytesAvailable?.let { Math.min(it, maxBufferSize) }
                val buffers = bufferSize?.let { ByteArray(it) }
                while (inputStream?.read(buffers).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream?.close()
                outputStream.close()
            } catch (e: Exception) {
                e.message?.let { Log.e("Exception", it) }
            }
            return file.path
        } else {
            return null
        }

    }

    /***
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    private fun copyFileToInternalStorage(uri: Uri, newDirName: String, context: Context): String? {
        val returnCursor: Cursor? = context.contentResolver.query(
            uri, arrayOf(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        )
        if (returnCursor != null) {
            /*
          * Get the column indexes of the data in the Cursor,
          *     * move to the first row in the Cursor, get the data,
          *     * and display it.
          * */
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val size = returnCursor.getLong(sizeIndex).toString()
            val output: File
            if (newDirName != "") {
                val dir = File(context.filesDir.toString() + "/" + newDirName)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                output = File(context.filesDir.toString() + "/" + newDirName + "/" + name)
            } else {
                output = File(context.filesDir.toString() + "/" + name)
            }
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(output)
                var read = 0
                val bufferSize = 1024
                val buffers = ByteArray(bufferSize)
                while (inputStream?.read(buffers).also {
                        if (it != null) {
                            read = it
                        }
                    } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream?.close()
                outputStream.close()
            } catch (e: Exception) {
                e.message?.let { Log.e("Exception", it) }
            }
            return output.path
        } else {
            return null
        }


    }

    private fun getFilePathForWhatsApp(uri: Uri, context: Context): String? {
        return copyFileToInternalStorage(uri, "whatsapp", context)
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(
                uri!!, projection,
                selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun isWhatsAppFile(uri: Uri): Boolean {
        return "com.whatsapp.provider.media" == uri.authority
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
    }


}