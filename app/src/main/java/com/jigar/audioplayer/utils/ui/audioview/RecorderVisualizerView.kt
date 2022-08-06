package com.jigar.audioplayer.utils.ui.audioview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


class RecorderVisualizerView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var amplitudes : MutableList<Float>? = null // amplitudes for line lengths
    private var width1 = 0
    private var height1 = 0

    private val linePaint: Paint = Paint()// specifies line drawing characteristics


    // called when the dimensions of the View change
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        width1 = w // new width of this View
        height1 = h // new height of this View
        amplitudes = ArrayList(height1 / LINE_WIDTH)
    }

    // clear all amplitudes to prepare for a new visualization
    fun clear() {
        amplitudes?.clear()
    }

    // add the given amplitude to the amplitudes ArrayList
    fun addAmplitude(amplitude: Int) {
        amplitudes?.add(amplitude.toFloat()) // add newest to the amplitudes ArrayList

        // if the power lines completely fill the VisualizerView
        if ((amplitudes?.size ?: (0 * LINE_WIDTH)) >= height1) {
            amplitudes?.removeAt(0) // remove oldest power value
        }
    }

    // draw the visualizer with scaled lines representing the amplitudes
    public override fun onDraw(canvas: Canvas) {
        val middle = height1 / 2 // get the middle of the View
        var curX = 0f // start curX at zero

        // for each item in the amplitudes ArrayList
        for (power in amplitudes!!) {
            val scaledHeight = power / LINE_SCALE // scale the power
            curX += LINE_WIDTH.toFloat() // increase X by LINE_WIDTH

            // draw a line representing this item in the amplitudes ArrayList
            canvas.drawLine(
                curX, middle + scaledHeight / 2, curX, middle
                        - scaledHeight / 2, linePaint
            )
        }
    }

    companion object {
        private const val LINE_WIDTH = 2 // width of visualizer lines
        private const val LINE_SCALE = 100 // scales visualizer lines
    }

    // constructor
    init {
        // create Paint for lines
        linePaint.color = Color.GREEN // set color to green
        linePaint.strokeWidth = LINE_WIDTH.toFloat() // set stroke width
    }
}