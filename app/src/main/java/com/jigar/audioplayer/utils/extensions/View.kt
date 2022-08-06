package com.jigar.audioplayer.utils.extensions

import android.view.View
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
inline fun <T : View> T.onClick(crossinline func: T.() -> Unit) {
    setOnClickListener { func() }
}
