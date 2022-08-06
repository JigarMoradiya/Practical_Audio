package com.jigar.audioplayer.utils.ui.audioview.util

import android.os.Environment
import java.io.File


class Helper private constructor() {

    fun getAllFileInDirectory(directory: File): ArrayList<String> {
        val files = directory.listFiles()
        val listOfRecordings = ArrayList<String>()
        if (files != null) {
            for (file in files) {
                if (file != null) {
                    if (file.isDirectory) { // it is a folder...
                        getAllFileInDirectory(file)
                    } else { // it is a file...
                        listOfRecordings.add(file.absolutePath)
                    }
                }
            }
        }
        return listOfRecordings
    }

    fun createRecordingFolder(): Boolean {
        return if (!File(RECORDING_PATH).exists()) {
            File(RECORDING_PATH).mkdir()
        } else {
            true
        }
    }

    companion object {
        var helperInstance: Helper? = null
            get() {
                if (null == field) {
                    field = Helper()
                }
                return field
            }
            private set
        val RECORDING_PATH = Environment
            .getExternalStorageDirectory().toString() + "/Recordings"
    }
}