package tech.soulike.yunzhan.cloudexhibition.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ProgressBar
import tech.soulike.yunzhan.cloudexhibition.R

/**
 * Created by thunder on 18-3-6.
 *
 */
open class HorizontalProgressBarWithNumber constructor(context: Context, attrs: AttributeSet,
                                                                defStyle: Int = 0) : ProgressBar(context, attrs, defStyle) {
    private var allCount: Int = 0
    private var current: Int = 0

    /**
     * painter of all drawing things
     */
    protected var mPaint = Paint()
    /**
     * color of progress number
     */
    protected var mTextColor = DEFAULT_TEXT_COLOR
    /**
     * size of text (sp)
     */
    protected var mTextSize = sp2px(DEFAULT_TEXT_SIZE)

    /**
     * offset of draw progress
     */
    protected var mTextOffset = dp2px(DEFAULT_SIZE_TEXT_OFFSET)

    /**
     * height of reached progress bar
     */
    protected var mReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_REACHED_PROGRESS_BAR)

    /**
     * color of reached bar
     */
    protected var mReachedBarColor = DEFAULT_TEXT_COLOR
    /**
     * color of unreached bar
     */
    protected var mUnReachedBarColor = DEFAULT_COLOR_UNREACHED_COLOR
    /**
     * height of unreached progress bar
     */
    protected var mUnReachedProgressBarHeight = dp2px(DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR)
    /**
     * view width except padding
     */
    protected var mRealWidth: Int = 0

    var tipTitle = ""

    protected var mIfDrawText = true

    fun setAllCount(allCount: Int) {
        this.allCount = allCount
    }

    fun setCurrent(current: Int) {
        this.current = current
    }


    init {

        isHorizontalScrollBarEnabled = true

        obtainStyledAttributes(attrs)

        mPaint.textSize = mTextSize.toFloat()
        mPaint.color = mTextColor

    }

    /**
     * get the styled attributes
     *
     * @param attrs
     */
    private fun obtainStyledAttributes(attrs: AttributeSet) {
        // init values from custom attributes
        val attributes = context.obtainStyledAttributes(
                attrs, R.styleable.HorizontalProgressBarWithNumber)

        mTextColor = attributes
                .getColor(
                        R.styleable.HorizontalProgressBarWithNumber_progress_text_color,
                        DEFAULT_TEXT_COLOR)
        mTextSize = attributes.getDimension(
                R.styleable.HorizontalProgressBarWithNumber_progress_text_size,
                mTextSize.toFloat()).toInt()

        mReachedBarColor = attributes
                .getColor(
                        R.styleable.HorizontalProgressBarWithNumber_progress_reached_color,
                        mTextColor)
        mUnReachedBarColor = attributes
                .getColor(
                        R.styleable.HorizontalProgressBarWithNumber_progress_unreached_color,
                        DEFAULT_COLOR_UNREACHED_COLOR)
        mReachedProgressBarHeight = attributes
                .getDimension(
                        R.styleable.HorizontalProgressBarWithNumber_progress_reached_bar_height,
                        mReachedProgressBarHeight.toFloat()).toInt()
        mUnReachedProgressBarHeight = attributes
                .getDimension(
                        R.styleable.HorizontalProgressBarWithNumber_progress_unreached_bar_height,
                        mUnReachedProgressBarHeight.toFloat()).toInt()
        mTextOffset = attributes
                .getDimension(
                        R.styleable.HorizontalProgressBarWithNumber_progress_text_offset,
                        mTextOffset.toFloat()).toInt()

        val textVisible = attributes
                .getInt(R.styleable.HorizontalProgressBarWithNumber_progress_text_visibility,
                        VISIBLE)
        if (textVisible != VISIBLE) {
            mIfDrawText = false
        }
        attributes.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        var heightMeasureSpec1 = heightMeasureSpec
        if (heightMode != View.MeasureSpec.EXACTLY) {

            val textHeight = mPaint.descent() + mPaint.ascent()
            val exceptHeight = (paddingTop.toFloat() + paddingBottom.toFloat() + Math
                    .max(Math.max(mReachedProgressBarHeight,
                            mUnReachedProgressBarHeight).toFloat(), Math.abs(textHeight))).toInt()

             heightMeasureSpec1 = View.MeasureSpec.makeMeasureSpec(exceptHeight,
                    View.MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec1)
    }

    @Synchronized override fun onDraw(canvas: Canvas) {
        canvas.save()
        //画笔平移到指定paddingLeft， getHeight() / 2位置，注意以后坐标都为以此为0，0
        canvas.translate(paddingLeft.toFloat(), (height / 2).toFloat())

        var noNeedBg = false
        //当前进度和总值的比例
        val radio = progress * 1.0f / max
        //已到达的宽度
        var progressPosX = (mRealWidth * radio).toInt().toFloat()
        //绘制的文本
        val text = "正在下载第" + current + "个文件，共" + allCount + "个文件"

        //拿到字体的宽度和高度
        val textWidth = mPaint.measureText(text)
        val textHeight = (mPaint.descent() + mPaint.ascent()) / 2

        //如果到达最后，则未到达的进度条不需要绘制
        if (progressPosX + textWidth > mRealWidth) {
            progressPosX = mRealWidth - textWidth
            noNeedBg = true
        }

        // 绘制已到达的进度
        val endX = progressPosX - mTextOffset / 2
        if (endX > 0) {
            mPaint.color = mReachedBarColor
            mPaint.strokeWidth = mReachedProgressBarHeight.toFloat()
            canvas.drawLine(0f, 0f, endX, 0f, mPaint)
        }

        // 绘制文本
        if (mIfDrawText) {
            mPaint.color = mTextColor
            canvas.drawText(text, progressPosX, -textHeight, mPaint)
        }

        // 绘制未到达的进度条
        if (!noNeedBg) {
            val start = progressPosX + (mTextOffset / 2).toFloat() + textWidth
            mPaint.color = mUnReachedBarColor
            mPaint.strokeWidth = mUnReachedProgressBarHeight.toFloat()
            canvas.drawLine(start, 0f, mRealWidth.toFloat(), 0f, mPaint)
        }

        canvas.restore()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRealWidth = w - paddingRight - paddingLeft

    }

    protected fun dp2px(dpVal: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal.toFloat(), resources.displayMetrics).toInt()
    }

    /**
     * sp 2 px
     *
     * @param spVal
     * @return
     */
    protected fun sp2px(spVal: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal.toFloat(), resources.displayMetrics).toInt()

    }

    companion object {
        private val DEFAULT_TEXT_SIZE = 10
        private val DEFAULT_TEXT_COLOR = -0xcc8549
        private val DEFAULT_COLOR_UNREACHED_COLOR = -0x2c2926
        private val DEFAULT_HEIGHT_REACHED_PROGRESS_BAR = 2
        private val DEFAULT_HEIGHT_UNREACHED_PROGRESS_BAR = 2
        private val DEFAULT_SIZE_TEXT_OFFSET = 10

        protected val VISIBLE = 0
    }
}