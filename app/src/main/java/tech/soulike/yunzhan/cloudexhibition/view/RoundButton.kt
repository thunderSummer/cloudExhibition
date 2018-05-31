package tech.soulike.yunzhan.cloudexhibition.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import tech.soulike.yunzhan.cloudexhibition.R

/**
 * Created by thunder on 18-3-6.
 */
class RoundButton :View {
    private var radius: Int = 0
    private var content: String? = null
    private var lineColor: Int = 0
    private var textSize: Int = 0
    private lateinit var mPaint: Paint
    private lateinit var bound: Rect
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var lineWidth: Int = 0


    constructor (context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet?):this(context,attrs,0)

     constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context,attrs,defStyleAttr) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.RoundButton, defStyleAttr, 0)
        val n = typedArray.indexCount
         (0 until n)
                 .map { typedArray.getIndex(it) }
                 .forEach {
                     when (it) {
                         R.styleable.RoundButton_lineColor -> lineColor = typedArray.getColor(it, Color.BLACK)
                         R.styleable.RoundButton_textSize -> textSize = typedArray.getDimensionPixelSize(it, TypedValue.applyDimension(
                         TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics).toInt())
                         R.styleable.RoundButton_text -> content = typedArray.getString(it)
                         R.styleable.RoundButton_radius -> radius = typedArray.getDimensionPixelSize(it, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt())
                         R.styleable.RoundButton_lineWidth -> lineWidth = typedArray.getDimensionPixelSize(it, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics).toInt())
                     }
                 }
        mPaint = Paint()
        mPaint.textSize = textSize.toFloat()
        mPaint.color = lineColor

        bound = Rect()
        //获取文字高度大小
        mPaint.getTextBounds(content, 0, content!!.length, bound)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        var specMode = View.MeasureSpec.getMode(widthMeasureSpec)
        var specSize = View.MeasureSpec.getSize(widthMeasureSpec)
        if (specMode == View.MeasureSpec.EXACTLY) {
            mWidth = specSize

        } else {
            if (specMode == View.MeasureSpec.AT_MOST) {
                mPaint.getTextBounds(content, 0, content!!.length, bound)
                mWidth = +bound.width() + radius * 2 + lineWidth
                Log.d("s", "onMeasure: " + mWidth)
            }
        }
        specMode = View.MeasureSpec.getMode(heightMeasureSpec)
        specSize = View.MeasureSpec.getSize(heightMeasureSpec)
        if (specMode == View.MeasureSpec.EXACTLY) {
            mHeight = specSize
        } else {
            if (specMode == View.MeasureSpec.AT_MOST) {
                mHeight = +radius * 2 + bound.height()
            }
        }
        setMeasuredDimension((mWidth * 1.2f).toInt(), (mHeight * 1.6f).toInt())
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d(" ddd ", "onDraw: " + content!!)
        mPaint.style = Paint.Style.STROKE
        mPaint.color = lineColor
        mPaint.strokeWidth = 3f
        Log.d("ss", "onDraw: " + mWidth + mHeight + radius)
        val rectf = RectF(mWidth * 0.1f, mHeight * 0.3f, mWidth * 1.1f, mHeight * 1.3f)

        //   canvas.drawRect(rectf,mPaint);
        canvas.drawText(content!!, mWidth * 0.6f - bound.width() * 1.0f / 2, mHeight * 0.8f + bound.height() * 0.3f, mPaint)
        mPaint.strokeWidth = 10f
        canvas.drawRoundRect(rectf, radius.toFloat(), radius.toFloat(), mPaint)
    }

    fun setRadius(radius: Int) {
        this.radius = radius
        requestLayout()
    }

    fun setContent(content: String) {
        this.content = content
        requestLayout()

    }

    fun setLineColor(lineColor: Int) {
        this.lineColor = lineColor
        requestLayout()
    }

    fun setTextSize(textSize: Int) {
        this.textSize = textSize
        requestLayout()
    }
}