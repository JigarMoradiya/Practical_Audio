package com.jigar.audioplayer.utils

import android.util.Log
import androidx.databinding.BindingAdapter
import com.google.android.material.textview.MaterialTextView
import java.util.*

@BindingAdapter("app:audioDuration")
fun setAudioDuration(view: MaterialTextView, audioDuration: Long?) {
    view.text = DateUtil.secToTimeFormat(audioDuration)
}
@BindingAdapter("app:fileTime")
fun setFileTime(view: MaterialTextView, fileTime: Date?) {
    view.text = DateUtil.getDateString(fileTime)
}