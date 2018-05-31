package tech.soulike.yunzhan.cloudexhibition.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import tech.soulike.yunzhan.cloudexhibition.R

/**
 * Created by thunder on 18-3-6.
 *
 */
class RoundProgressBarWidthNumber constructor(context: Context, attrs: AttributeSet ) : HorizontalProgressBarWithNumber(context, attrs) {
    var isCancel = false
    private var mRadius = dp2px(30)
    private lateinit var recf: RectF


    init {

        mReachedProgressBarHeight = (mUnReachedProgressBarHeight * 2.5f).toInt()
        val ta = context.obtainStyledAttributes(attrs,
                R.styleable.RoundProgressBarWidthNumber)
        mRadius = ta.getDimension(
                R.styleable.RoundProgressBarWidthNumber_Radius, mRadius.toFloat()).toInt()
        ta.recycle()

        mTextSize = sp2px(14)

        mPaint.style = Paint.Style.STROKE
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.strokeCap = Paint.Cap.ROUND
        recf = RectF(0f, 0f, (mRadius * 2).toFloat(), (mRadius * 2).toFloat())

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightMeasureSpec1 = heightMeasureSpec
        var widthMeasureSpec1 = widthMeasureSpec
        val paintWidth = Math.max(mReachedProgressBarHeight,
                mUnReachedProgressBarHeight)

        if (heightMode != MeasureSpec.EXACTLY) {

            heightMeasureSpec1 = MeasureSpec.makeMeasureSpec((paddingTop + paddingBottom
                    + mRadius * 2 + paintWidth),
                    MeasureSpec.EXACTLY)
        }
        if (widthMode != MeasureSpec.EXACTLY) {
            val exceptWidth = (paddingLeft + paddingRight
                    + mRadius * 2 + paintWidth) as Int
            widthMeasureSpec1 = MeasureSpec.makeMeasureSpec(exceptWidth,
                    MeasureSpec.EXACTLY)
        }

        super.onMeasure(widthMeasureSpec1, heightMeasureSpec1)

    }

    @Synchronized override fun onDraw(canvas: Canvas) {
        var text = tipTitle
        if (!isCancel) {
            text = text + progress + "%"
        }
        // mPaint.getTextBounds(text, 0, text.length(), mTextBound);
        val textWidth = mPaint.measureText(text)
        val textHeight = (mPaint.descent() + mPaint.ascent()) / 2

        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        mPaint.style = Paint.Style.STROKE
        // draw unreaded bar
        mPaint.color = mUnReachedBarColor
        mPaint.strokeWidth = mUnReachedProgressBarHeight.toFloat()
        canvas.drawCircle(mRadius.toFloat(), mRadius.toFloat(), mRadius.toFloat(), mPaint)
        // draw reached bar
        mPaint.color = mReachedBarColor
        mPaint.strokeWidth = mReachedProgressBarHeight.toFloat()
        val sweepAngle = progress * 1.0f / max * 360
        canvas.drawArc(recf, 0f,
                sweepAngle, false, mPaint)
        // draw text
        mPaint.style = Paint.Style.FILL
        canvas.drawText(text, mRadius - textWidth / 2, mRadius - textHeight,
                mPaint)

        canvas.restore()

    }

}