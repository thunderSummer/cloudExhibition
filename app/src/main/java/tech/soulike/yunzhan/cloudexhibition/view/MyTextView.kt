package tech.soulike.yunzhan.cloudexhibition.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextPaint
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import tech.soulike.yunzhan.cloudexhibition.R

/**
 * Created by thunder on 18-3-6.
 *
 */
class MyTextView :  android.support.v7.widget.AppCompatEditText {
    // 画下划线的画笔
    private lateinit var paint: Paint
    // 下划线的开始y坐标
    private var lineStartY: Int = 0
    // 下划线的初始颜色
    private var preLineColor: Int = 0

    // 额外的顶部内边距
    private var extraTopPadding: Int = 0
    // 额外的底部内边距
    private var extraBottomPadding: Int = 0

    // 绘制标签的画笔
    private lateinit var textPaint: TextPaint
    // 标签的透明度
    private var textAlpha = 0f
    // 标签的颜色
    private var labelColor: Int = 0
    // 标签的文字内容
    private lateinit var labelText: String
    // 超过限制长度时的下划线颜色
    private var overLengthColor: Int = 0
    // 最大输入的字符数
    private var maxCount: Int = 0
    // 当前输入的字符数
    private var presentCount: Int = 0
    // 标签文字的动画
    internal lateinit var labelAnim: ValueAnimator
    // 标签是否展现的标志
    private var isShow = false
    // 标签动画中标签移动的比例
    private var yFraction: Float = 0.toFloat()


    // 用来获取计数结果的字符串
    private var countString: StringBuffer? = null
    // 绘制计数结果的画笔
    private var countPaint: TextPaint? = null
    private var allowDo: Boolean = false
    private var limitNum: Int = 0


    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {

        init(attrs) // 初始化
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {

        init(attrs) // 初始化
    }

    // 初始化各个数据
    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.MyTextView)
            // 获取设置的最大输入字符数，默认为-1，-1时不限制输入
            maxCount = array.getInteger(R.styleable.MyTextView_maxLength, -1)
            // 捕捉焦点后，下划线的初始颜色
            preLineColor = array.getColor(R.styleable.MyTextView_preLineColor, ContextCompat.getColor(context,R.color.colorBlackThree))
            // 标签文字的颜色
            labelColor = array.getColor(R.styleable.MyTextView_labelColor, ContextCompat.getColor(context,R.color.colorBlackThree))
            // 标签文字的内容
            labelText = array.getString(R.styleable.MyTextView_labelText)
            limitNum = array.getInteger(R.styleable.MyTextView_limitCount, -1)

            // 超过长度后的下划线颜色
            overLengthColor = array.getColor(R.styleable.MyTextView_overlengthColor, Color.RED)

            array.recycle()

            // 用来存放右下角的计数内容的字符串
            countString = StringBuffer()

            // 初始化下划线画笔
            paint = Paint()
            paint.color = Color.LTGRAY
            paint.isAntiAlias = true
            paint.strokeWidth = dpToPix(0.35f).toFloat()
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeCap = Paint.Cap.ROUND

            // 初始化标签画笔
            textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            textPaint.textSize = spToPix(14.5f)
            textPaint.color = labelColor

            // 初始化字符计数的画笔
            countPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            countPaint!!.textSize = spToPix(10f)
            countPaint!!.color = Color.LTGRAY

            // 获取额外的顶部内边距
            extraTopPadding = getTextHeight(textPaint).toInt()

            // 获取额外的底部内布距
            extraBottomPadding = getTextHeight(textPaint).toInt()

            // 矫正edittext的内边距
            correctPaddings()

            // 初始化标签动画
            labelAnim = ValueAnimator.ofFloat(0f, 255f)
            labelAnim.addUpdateListener { animation ->
                // 获取标签的透明度
                textAlpha = animation.animatedValue as Float
                // 获取文字的高度比例：
                // 当透明度为0时，高度比例为1.5，为1时，高度为1
                // 达到一种从底部浮现的效果
                yFraction = (-(5.0f / 2550.0f) * textAlpha + 1.5).toFloat()
                // 重绘
                invalidate()
            }

            // 初始化监听器
            initListener()
            // 设置提示字体颜色
            setHintTextColor(Color.LTGRAY)
            // 设置文字方向

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                gravity = View.SCROLL_INDICATOR_LEFT
            }

        }
    }


    private fun initListener() {

        /// 设置edittetx内容改变监听器
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (limitNum != -1) {
                    allowDo = s.length > limitNum
                }
                Log.d("ss", "afterTextChanged: " + allowDo + limitNum)

                // 当内容改变后，计数、并判断是否超过规定字符长度
                if (maxCount != -1) {
                    // 添加计数字符串
                    countString!!.delete(0, countString!!.length)
                    presentCount = s.length
                    countString!!.append(presentCount)
                    countString!!.append(" / ")
                    countString!!.append(maxCount)

                    // 超过规定长度时，绘制的颜色发生变化
                    if (presentCount > maxCount) {
                        paint.color = overLengthColor
                        countPaint!!.color = overLengthColor
                    } else {
                        paint.color = preLineColor
                        countPaint!!.color = Color.LTGRAY
                    }
                }

                // 当内容长度为0并获得焦点时：
                if (s.length == 0 && isShow && isFocused) {
                    // 将标签动画逆序播放，将标签隐藏
                    labelAnim.reverse()
                    isShow = false

                } else if (s.length != 0 && !isShow && isFocused) {
                    // 当内容长度不为0时，播放标签浮出一次
                    labelAnim.start()
                    isShow = true

                }
            }
        })

        // 添加焦点的获取通知
        onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // 改变下划线的粗细
                paint.strokeWidth = dpToPix(1.3f).toFloat()
                // 改变标签的颜色
                textPaint.color = labelColor

                if (presentCount > maxCount && maxCount != -1) {
                    // 超出字符长度时，设置画笔颜色
                    paint.color = overLengthColor
                    countPaint!!.color = overLengthColor
                } else {
                    // 不超出字符长度/不设置规定长度时，设置画笔颜色
                    paint.color = preLineColor
                    countPaint!!.color = Color.LTGRAY
                }

            } else {
                // 没有获取焦点时，改变下划线的颜色和粗细和标签颜色
                textPaint.color = Color.LTGRAY
                paint.color = Color.LTGRAY
                paint.strokeWidth = dpToPix(0.35f).toFloat()
            }
        }
    }


    // sp to pix
    private fun spToPix(i: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, i, context.resources.displayMetrics)
    }

    // 获取文字的高度
    private fun getTextHeight(paint: Paint): Float {
        val fontMetrics = paint.fontMetrics
        return fontMetrics.bottom - fontMetrics.descent - fontMetrics.ascent
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 设置edittext的背景为空，主要为了隐藏自带的下划线
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            background = null
        } else {
            setBackgroundDrawable(null)
        }

    }


    override fun onDraw(canvas: Canvas) {

        // 获取下划线的起点高度
        lineStartY = scrollY + height - paddingBottom + dpToPix(5f)
        // 设置标签的透明度
        textPaint.alpha = textAlpha.toInt()
        // 根据标签的高度比例绘制标签文字
        canvas.drawText(labelText, scrollX.toFloat(), (scrollY - dpToPix(1f) + extraTopPadding) * yFraction, textPaint)
        // 绘制下划线
        canvas.drawRect(scrollX.toFloat(), lineStartY.toFloat(), (scrollX + width - paddingRight).toFloat(), (lineStartY + dpToPix(0.8f)).toFloat(), paint!!)
        // 根据是否有字符长度规定绘制右下角的计数
        if (maxCount != -1) {
            canvas.drawText(countString!!.toString(), (scrollX + width - paddingRight - getTextWidth(countString!!.toString(), countPaint)).toFloat(), (lineStartY + extraBottomPadding).toFloat(), countPaint!!)
        }
        // 开始edittext原生的绘制
        super.onDraw(canvas)

    }

    // 获取字符串的宽度
    private fun getTextWidth(text: String, paint: TextPaint?): Int {
        return paint!!.measureText(text).toInt()
    }

    // dp to px
    private fun dpToPix(i: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i, context.resources.displayMetrics).toInt()
    }


    // 因为我们需要绘制标签和下划线，因此需要重新设置padding值
    private fun correctPaddings() {

        super.setPadding(paddingLeft, paddingTop + extraTopPadding, paddingRight, paddingBottom + extraBottomPadding + dpToPix(20f))

    }

    fun getAllowDo(): Boolean {
        return allowDo
    }
}