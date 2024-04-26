package com.example.tablerecognizer.ui.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.example.tablerecognizer.R


class ResizableFrameLayout : FrameLayout {
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    private val borderPaint = Paint()
    private fun init(context: Context) {
        borderPaint.color = Color.WHITE
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = resources.getInteger(R.integer.crop_frame_stroke_width).toFloat()
    }

    private var centerX = 0
    private var centerY = 0
    private var maxWidth = 0
    private var maxHeight = 0
    private var minWidth = 0
    private var minHeight = 0
    private val cornerLength = resources.getInteger(R.integer.crop_frame_corner_length).toFloat()
    private val location = IntArray(2)
    public override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setUpCorners(canvas)

        this.getLocationOnScreen(location)

        centerX = location[0] + width/2
        centerY = location[1] + height/2
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Горизонтальная ориентация
            maxWidth = resources.getInteger(R.integer.crop_frame_horizontal_orientation_max_width)
            minWidth = resources.getInteger(R.integer.crop_frame_horizontal_orientation_min_width)
            maxHeight= resources.getInteger(R.integer.crop_frame_horizontal_orientation_max_height)
            minHeight = resources.getInteger(R.integer.crop_frame_horizontal_orientation_min_height)
        } else {
            // Вертикальная ориентация
            maxWidth = resources.getInteger(R.integer.crop_frame_vertical_orientation_max_width)
            minWidth = resources.getInteger(R.integer.crop_frame_vertical_orientation_min_width)
            maxHeight= resources.getInteger(R.integer.crop_frame_vertical_orientation_max_height)
            minHeight = resources.getInteger(R.integer.crop_frame_vertical_orientation_min_height)
        }
    }
    private fun setUpCorners(canvas: Canvas?){
        canvas?.drawLine(0f, 0f, 0f, cornerLength, borderPaint)
        canvas?.drawLine(0f, 0f, cornerLength, 0f, borderPaint)

        canvas?.drawLine(width.toFloat(), 0f, width.toFloat(), cornerLength, borderPaint)
        canvas?.drawLine(width.toFloat(), 0f, width.toFloat()-cornerLength, 0f, borderPaint)

        canvas?.drawLine(width.toFloat(), height.toFloat(), width.toFloat(), height.toFloat() - cornerLength, borderPaint)
        canvas?.drawLine(width.toFloat(), height.toFloat(), width.toFloat()-cornerLength, height.toFloat(), borderPaint)

        canvas?.drawLine(0f, height.toFloat(), 0f, height.toFloat() - cornerLength, borderPaint)
        canvas?.drawLine(0f, height.toFloat(), cornerLength, height.toFloat(), borderPaint)
    }
    var lastTouchX :Int = 0
    var lastTouchY :Int = 0
    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN ->{
                lastTouchX = event.rawX.toInt()
                lastTouchY = event.rawY.toInt()
                val a = 0
            }

            MotionEvent.ACTION_MOVE -> {
                val x: Int = event.rawX.toInt()
                val y: Int = event.rawY.toInt()

                var distanceX =0;
                var distanceY =0;

                when
                {
                    //от 1 до 3 часов
                    x > centerX && y < centerY -> {
                        distanceX = x - lastTouchX
                        distanceY = lastTouchY - y
                    }
                    //от 3 до 6 часов
                    x > centerX && y > centerY -> {
                        distanceX = x - lastTouchX
                        distanceY = y - lastTouchY
                    }
                    //от 6 до 9 часов
                    x < centerX && y > centerY -> {
                        distanceX = lastTouchX - x
                        distanceY = y - lastTouchY
                    }
                    //от 9 до 12 часов
                    x < centerX && y < centerY -> {
                        distanceX = lastTouchX - x
                        distanceY = lastTouchY - y
                    }
                }

                var newWidth = width + distanceX * 2
                var newHeight = height + distanceY * 2

                when
                {
                    newWidth < minWidth -> {
                        newWidth = minWidth
                    }
                    newWidth > maxWidth -> {
                        newWidth = maxWidth
                    }
                }
                when
                {
                    newHeight < minHeight -> {
                        newHeight = minHeight
                    }
                    newHeight > maxHeight -> {
                        newHeight = maxHeight
                    }
                }

                layoutParams.width  = newWidth
                layoutParams.height = newHeight

                lastTouchX = x
                lastTouchY = y

                setLayoutParams(layoutParams)
            }
        }

        return true
    }
}