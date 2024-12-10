package com.ebrahimamin.loadingstatusbaranimation

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var progressValue = 0.0F

    private val paint = Paint().apply { isAntiAlias = true }
    private val textPaint = Paint().apply {
        textSize = context.resources.getDimension(R.dimen.default_text_size)
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }
    private val circlePaint = Paint().apply { isAntiAlias = true }

    private var loadingButtonText: String = "Loading"
    private var completeButtonText: String = "Complete"
    private var loadingButtonColor: Int = Color.BLUE
    private var completeButtonColor: Int = Color.GREEN
    private var arcColor: Int = Color.YELLOW

    private var text: String = completeButtonText

    private var valueAnimator = ValueAnimator()

    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Clicked -> {}
            ButtonState.Loading -> {
                text = loadingButtonText
                valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
                    duration = 2500L
                    repeatCount = ValueAnimator.INFINITE
                    addUpdateListener {
                        progressValue = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
                text = completeButtonText
                progressValue = 0.0F
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.let {
            it.drawColor(completeButtonColor)
            drawRectangle(it)
            drawText(it)
            drawArc(it)
        }
    }

    private fun drawRectangle(canvas: Canvas) {
        val rectDraw = RectF(0F, 0F, widthSize * progressValue, heightSize.toFloat())
        paint.color = loadingButtonColor
        canvas.drawRect(rectDraw, paint)
    }

    private fun drawText(canvas: Canvas) {
        canvas.drawText(text, widthSize / 2F, heightSize / 2F + 10, textPaint)
    }

    private fun drawArc(canvas: Canvas) {
        val circleRadius = resources.getDimension(R.dimen.default_circle_radius)
        circlePaint.color = arcColor
        val xPosition = 3 * widthSize / 4F
        val yPosition = heightSize / 2F
        canvas.drawArc(
            xPosition, yPosition - circleRadius,
            circleRadius * 2 + xPosition, circleRadius + yPosition,
            0F, 360F * progressValue, true, circlePaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0)
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}