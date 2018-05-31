package tech.soulike.yunzhan.cloudexhibition.view

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import tech.soulike.yunzhan.cloudexhibition.base.MyApplication
import tech.soulike.yunzhan.cloudexhibition.listener.ImageChangeListener
import tech.soulike.yunzhan.cloudexhibition.util.FileUtil
import java.io.File
import java.util.*

/**
 * Created by thunder on 18-3-7.
 *
 */
class BaseImage : View {
    private val TAG  = "BaseImage"
    private lateinit var imageChangeListener: ImageChangeListener
    private var random: Random? = null
    private val file = File(FileUtil.getResourceHome() + "resource/")
    private lateinit var resourceFactory: ResourceFactory
    private lateinit var currentCanvas: Canvas
    private lateinit var currentBitmap: Bitmap
    private lateinit var nextBitmap: Bitmap
    private lateinit var nextCanvas: Canvas
    var current: Int = 0
        private set
    private var before = -1
    private lateinit var handler: BaseImageHandle
    private var start: Int = 0
    private val count1 = 10
    private var current1 = 0
    var time: Long = 10000
    private var flag: Int = 0
    private lateinit var camera: Camera
    private var matrix1: Matrix? = null
    private var centerX: Float = 0.toFloat()
    private var centerY: Float = 0.toFloat()
    private var extra: Float = 0.toFloat()
    private val max = 4
    internal var scale = 1f
    private var isNeedNext: Boolean = false
    private val message = Message()
    inner class BaseImageHandle : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                START_PLAY -> {
                    current1++
                    if (current1 > count1) {
                        current1 = 0
                    }
                    postInvalidate()
                }
                3 -> postInvalidate()
                END_PLAY ->{
                    imageChangeListener.onBitmapPause()
                }
                
            }
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    fun setresourceFactory() {
        resourceFactory.initBitmapList()
    }

    @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {}

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
            resourceFactory.current = current
            if (resourceFactory.onDraw(currentCanvas)){
                resourceFactory.current = if (current >= resourceFactory.end || current < start) start else current+1
                if (resourceFactory.onDraw(nextCanvas)){
                    if (file.listFiles() != null) {
                        when (flag) {
                            0 -> drawRotateCanvas(canvas)
                            1 -> drawUpCanvas(canvas)
                            2 -> drawCanvas1(canvas)
                            3 -> drawScaleCanvas(canvas)
                        }
                    }
                }else{
                   canvas.drawBitmap(currentBitmap,0f,0f,Paint())
                    message.what = END_PLAY
                    handler.sendMessageDelayed(message, resourceFactory.getTime(current).toLong())
                }

            }else {
                imageChangeListener.onBitmapPause()
            }

    }

    private fun drawCanvas1(canvas: Canvas) {
        canvas.drawBitmap(currentBitmap, 0f, 0f, Paint())
        val height = canvas.height
        val width = canvas.width / count1
        if (current1 != 0) {
            canvas.run {
                save()
                clipRect(0, 0, width * current1, height)
                drawBitmap(nextBitmap, 0f, 0f, Paint())
                restore()
            }
        }
        when {
            current1 == 0 -> {
                val message = Message()
                message.what = START_PLAY
                flag = random!!.nextInt(max)
                handler.sendMessageDelayed(message, resourceFactory.getTime() * 1000.toLong())
            }
            count1 > current1 -> {
                val message = Message()
                message.what = START_PLAY
                handler.sendMessageDelayed(message, 10)
            }
            count1 <= current1 -> {
                current++
                if (current >= resourceFactory.end || current < start) {
                    current = start
                }
                current1 = 0
                handler.removeMessages(START_PLAY)
                postInvalidate()
            }
        }

    }

    fun setImageChangeListener(imageChangeListener: ImageChangeListener) {
        this.imageChangeListener = imageChangeListener
    }

    fun restart() {
        handler.removeMessages(START_PLAY)
        init()
    }

    private fun init() {
        currentBitmap = Bitmap.createBitmap(MyApplication.context.resources.displayMetrics.widthPixels, MyApplication.context.resources.displayMetrics.heightPixels, Bitmap.Config.ARGB_8888)
        nextBitmap = Bitmap.createBitmap(MyApplication.context.resources.displayMetrics.widthPixels, MyApplication.context.resources.displayMetrics.heightPixels, Bitmap.Config.ARGB_8888)
        currentCanvas = Canvas(currentBitmap)
        nextCanvas = Canvas(nextBitmap)
        resourceFactory = ResourceFactory()
        resourceFactory.initBitmapList()
        handler = BaseImageHandle()
        current = 0
        start = 0
        random = Random()
        camera = Camera()
        centerX = (currentBitmap.width / 2).toFloat()
        centerY = (currentBitmap.height / 2).toFloat()
        matrix1 = Matrix()
        extra = 0f
        isNeedNext = true
        if (file.listFiles() != null) {
//            resourceFactory.current = current
//            resourceFactory.onDraw(currentCanvas)
//            resourceFactory.current=(current + 1)
//            resourceFactory.onDraw(nextCanvas)
            postInvalidate()
        }
    }

    private fun drawUpCanvas(canvas: Canvas) {
        canvas.drawBitmap(currentBitmap, 0f, 0f, Paint())
        val height = canvas.height / count1
        val width = canvas.width
        if (current1 != 0) {
            canvas.save()
            canvas.clipRect(0, 0, width, height * current1)
            canvas.drawBitmap(nextBitmap, 0f, 0f, Paint())
            canvas.restore()
        }

        when {
            current1 == 0 -> {

                val message = Message()
                message.what = START_PLAY
                flag = random!!.nextInt(max)
                handler.sendMessageDelayed(message, resourceFactory.getTime() * 1000.toLong())
            }
            count1 > current1 -> {
                val message = Message()
                message.what = START_PLAY
                handler.sendMessageDelayed(message, 10)
            }
            count1 <= current1 -> {
                current++
                if (current >= resourceFactory.end || current < start) {
                    current = start
                }
                current1 = 0

                handler.removeMessages(START_PLAY)
                postInvalidate()
            }
        }
    }

    private fun drawRotateCanvas(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)
        matrix1!!.reset()
        camera.save()
        val rotate = (90 / count1).toFloat()
        camera.rotateX(rotate * current1 + extra)
        camera.getMatrix(matrix)
        camera.restore()
        val mValues = FloatArray(9)
        //        matrix.getValues(mValues);                //获取数值
        //        mValues[6] = mValues[6]/scale;            //数值修正
        //        mValues[7] = mValues[7]/scale;            //数值修正
        //        matrix.setValues(mValues);                //重新赋值
        matrix1!!.preTranslate(-this.centerX, -this.centerY)
        matrix1!!.postTranslate(this.centerX, this.centerY)
        canvas.concat(matrix1)
        if (current1 == 0) {
            if (extra == 0f) {
                val message = Message()
                message.what = START_PLAY
                flag = random!!.nextInt(max)
                handler.sendMessageDelayed(message, resourceFactory.getTime() * 1000.toLong())
            } else {
                val message = Message()
                message.what = START_PLAY
                handler.sendMessageDelayed(message, 200)
            }

        } else if (current1 < count1) {
            val message = Message()
            message.what = START_PLAY
            handler.sendMessageDelayed(message, 50)
        } else if (current1 == count1) {
            current1 = 0
            if (extra == 0f) {
                extra = 270f
                current++
                if (current >= resourceFactory.end || current < start) {
                    current = start
                }
            } else {
                extra = 0f
            }
            handler.removeMessages(START_PLAY)
            postInvalidate()
        }
        canvas.drawBitmap(currentBitmap, 0f, 0f, Paint())
    }

    private fun drawScaleCanvas(canvas: Canvas) {
        canvas.save()
        canvas.translate(centerX, centerY)
        val scale = (1.0 / count1).toFloat()
        if (isNeedNext) {
            canvas.scale(1 - scale * current1, 1 - scale * current1)
        } else {
            canvas.scale(scale * current1, scale * current1)
        }

        if (current1 == 0) {
            if (isNeedNext) {
                val message = Message()
                message.what = START_PLAY
                flag = random!!.nextInt(max)
                handler.sendMessageDelayed(message, (resourceFactory.getTime() * 1000).toLong())
            } else {
                val message = Message()
                message.what = START_PLAY
                handler.sendMessageDelayed(message, 200)
            }

        } else if (current1 == count1) {
            current1 = 0

            if (isNeedNext) {
                isNeedNext = false
                current++
                if (current >= resourceFactory.end || current < start) {
                    current = start
                }
            } else {
                isNeedNext = true
            }
            handler.removeMessages(START_PLAY)
            postInvalidate()
        } else if (current1 < count1) {
            val message = Message()
            message.what = START_PLAY
            handler.sendMessageDelayed(message, 50)
        }
        canvas.drawBitmap(currentBitmap, -centerX, -centerY, Paint())
    }
    public fun wakePlay(){
        current++
        postInvalidate()
    }

    companion object {
        private const val START_PLAY = 2
        private const val END_PLAY = 1
        private fun fresh() {

        }
    }
}