package com.jigar.audioplayer.utils

import android.Manifest

object AppConstants {
    const val READ_EXTERNAL_STORAGE: String = Manifest.permission.READ_EXTERNAL_STORAGE
    const val WRITE_EXTERNAL_STORAGE: String = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val RECORD_AUDIO: String = Manifest.permission.RECORD_AUDIO
    const val MIME_TYPE_AUDIO = "audio/*"
}