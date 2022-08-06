package com.jigar.audioplayer.utils.ui.audioview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.audiofx.Visualizer.OnDataCaptureListener
import android.view.View


/**
 * code taken from official sample app see
 * ApiDemos>src>com>example>android>apis>media>AudioFxDemo.java A simple class
 * that draws waveform data received from a
 * [OnDataCaptureListener.onWaveFormDataCapture]
 */
class PlayerVisualizerView(context: Context?) : View(context) {
    private var mBytes: ByteArray? = null
    private var mPoints: FloatArray? = null
    private val mRect = Rect()
    private val mForePaint = Paint()
    private fun init() {
        mBytes = null
        mForePaint.strokeWidth = 1f
        mForePaint.isAntiAlias = true
        mForePaint.color = Color.GREEN
    }

    fun updateVisualizer(bytes: ByteArray?) {
        mBytes = bytes
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBytes == null) {
            return
        }
        if (mPoints == null || mPoints!!.size < mBytes!!.size * 4) {
            mPoints = FloatArray(mBytes!!.size * 4)
        }
        mRect[0, 0, width] = height
        for (i in 0 until mBytes!!.size - 1) {
            mPoints!![i * 4] = (mRect.width() * i / (mBytes!!.size - 1)).toFloat()
            mPoints!![i * 4 + 1] = (mRect.height() / 2
                    + (mBytes!![i] + 128).toByte() * (mRect.height() / 2) / 128).toFloat()
            mPoints!![i * 4 + 2] = (mRect.width() * (i + 1) / (mBytes!!.size - 1)).toFloat()
            mPoints!![i * 4 + 3] = (mRect.height() / 2
                    + (mBytes!![i + 1] + 128).toByte() * (mRect.height() / 2)
                    / 128).toFloat()
        }
        canvas.drawLines(mPoints!!, mForePaint)
    }

    init {
        init()
    }
}