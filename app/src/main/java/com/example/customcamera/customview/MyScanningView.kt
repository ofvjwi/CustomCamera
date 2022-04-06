package com.example.customcamera.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View

class MyScanningView : View {
    private val paint: Paint = Paint()
    private var mPosY: Int = 0
    private var runAnimation: Boolean = true
    private var showLine: Boolean = true
    private var myHandler: Handler? = null
    private var refreshRunnable: Runnable? = null
    private var isGoingDown: Boolean = true
    private var mHeight: Int? = null


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        paint.color = Color.WHITE
        paint.strokeWidth = 3.0f
        //Add anything else you want to customize your line, like the stroke width
        myHandler = Handler(Looper.getMainLooper())
        refreshRunnable = Runnable { refreshView() }
    }

    override fun onDraw(canvas: Canvas?) {
        mHeight = height
        if (showLine) {
            canvas?.drawLine(0F, mPosY.toFloat(), width.toFloat(), mPosY.toFloat(), paint)
        }
        if (runAnimation) {
            refreshRunnable?.let { handler.postDelayed(it, 0) }
        }
    }

    fun startAnimation() {
        runAnimation = true
        showLine = true
        this.invalidate()
    }

    fun stopAnimation() {
        runAnimation = false
        showLine = false
        reset()
        this.invalidate()
    }

    private fun reset() {
        mPosY = 0
        isGoingDown = true
    }

    private fun refreshView() {
        //Update new position of the line
        if (isGoingDown) {
            mPosY += 5
            if (mPosY > mHeight!!) {
                mPosY = mHeight as Int
                isGoingDown = false
            }
        } else {
            //We invert the direction of the animation
            mPosY -= 5
            if (mPosY < 0) {
                mPosY = 0
                isGoingDown = true
            }
        }
        this.invalidate()
    }
}

