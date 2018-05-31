package tech.soulike.yunzhan.cloudexhibition.view

import android.graphics.*
import org.litepal.crud.DataSupport
import tech.soulike.yunzhan.cloudexhibition.base.MD5
import tech.soulike.yunzhan.cloudexhibition.base.MyApplication
import tech.soulike.yunzhan.cloudexhibition.base.ResourceData
import tech.soulike.yunzhan.cloudexhibition.util.FileUtil
import tech.soulike.yunzhan.cloudexhibition.util.LogUtil
import tech.soulike.yunzhan.cloudexhibition.util.SharedPreferenceUtil
import tech.soulike.yunzhan.cloudexhibition.util.StringUtil
import java.io.File
import java.util.ArrayList
import javax.sql.DataSource
import android.graphics.Bitmap
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by thunder on 18-3-7.
 */
class ResourceFactory {
    private lateinit var bitmapPaint: Paint
    private lateinit var bitmap: Bitmap
    var start: Int = 0
        set(value) {
            field = value
            SharedPreferenceUtil.putInt(StringUtil.START_PICTURE, start)
        }
    var end: Int = 0
        set(value)  {
            field = value
            SharedPreferenceUtil.putInt(StringUtil.END_PICTURE, field)
        }
    var current: Int = 0
        set(value) =  if (value >= bitmapList.size) field=0 else field=value
    private lateinit var integers: MutableList<Int>
    private var bitmapList: MutableList<Bitmap?> = ArrayList()
    private lateinit var timeOrder: MutableList<Int>
    private lateinit var files: Array<File>
    fun initBitmapList() {
        bitmapPaint = Paint()
        val i = 0
            integers = ArrayList()
            bitmapList = ArrayList()
            timeOrder = ArrayList()


        val file = File(FileUtil.getResourceHome() + "resource/")

        files = file.listFiles()
        val resourceDataList = DataSupport.findAll(ResourceData::class.java)
        for (file1 in files) {
            if (fileFilter(file1)){
                if (file1.exists()) {
                    val resourceData = resourceDataList.firstOrNull{
                        it.adMd5==MD5.getFileMD5String(file1)
                    }
                    if (resourceData!=null) {
                        timeOrder.add(resourceData.adTime)
                        val bitmapOld = android.graphics.BitmapFactory.decodeFile(file1.absolutePath)
                        val bitmapBase = Bitmap.createBitmap(MyApplication.context.resources.displayMetrics.widthPixels, MyApplication.context.resources.displayMetrics.heightPixels, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmapBase)
                        val fileQr = File(FileUtil.getResourceHome()+"qrcode/qrcode_"+resourceData.adId)

                        canvas.drawColor(Color.BLACK)

                        val widthOld = bitmapOld.width
                        val heightOld = bitmapOld.height
                        val widthNew = MyApplication.context.resources.displayMetrics.widthPixels
                        val heightNew = MyApplication.context.resources.displayMetrics.heightPixels
                        val scaleWidth = widthNew.toFloat() / widthOld
                        val scaleHeight = heightNew.toFloat() / heightOld
                        val matrix = Matrix()
                        val min = if (scaleHeight > scaleHeight) scaleHeight else scaleWidth
                        matrix.postScale(min, min)
                        val newBm = Bitmap.createBitmap(bitmapOld, 0, 0, widthOld, heightOld, matrix, true)
                        canvas.drawBitmap(newBm, (widthNew - widthOld * min) / 2, (heightNew - heightOld * min) / 2, Paint())
                        if (fileQr.exists()){
                            val bitmapQr = android.graphics.BitmapFactory.decodeFile(fileQr.absolutePath)
                            saveMyBitmap(bitmapQr,fileQr.name+"new")
                            matrix.postScale(100f/bitmapQr.width,100f/bitmapQr.height)

                            val newBitmapQr = Bitmap.createBitmap(bitmapQr, 0, 0, bitmapQr.width, bitmapQr.height, matrix, true)
                            canvas.drawBitmap(newBitmapQr,widthNew/10.toFloat(),heightNew/10.toFloat(), Paint())
                        }
                        bitmapList.add(bitmapBase)

                    }else{
                        FileUtil.deleteFile(file1)
                    }
                }else{
                    bitmapList.add(null)
                    timeOrder.add(SharedPreferenceUtil.getInt(file1.name, 0))
                }
            }

        }
        SharedPreferenceUtil.putInt(StringUtil.END_PICTURE, bitmapList.size)
        start = 0
        end = bitmapList.size
        this.current = SharedPreferenceUtil.getInt(StringUtil.CURRENT_PICTURE, start)
    }
    private fun fileFilter(file:File):Boolean{
        val filePost = listOf(".jpg",".png")
        return filePost.contains(file.name.substring(file.name.lastIndexOf('.')))
    }

    fun addBitmap(path: String) {
        bitmapList.add(android.graphics.BitmapFactory.decodeFile(path))
        end++
        SharedPreferenceUtil.putInt(StringUtil.END_PICTURE, end)
    }

    private fun createBitmap() :Boolean {
        if (bitmapList.size > 0)
            bitmap = bitmapList[current] ?: return false
        return true
    }

    fun getTime(): Int {
        return timeOrder[current]
    }
    fun getTime(current: Int):Int{
        return timeOrder[current]
    }
    internal fun onDraw(canvas: Canvas) : Boolean {
        return if (createBitmap()){
            canvas.drawBitmap(bitmap, 0f, 0f, bitmapPaint)
            SharedPreferenceUtil.putInt(StringUtil.CURRENT_PICTURE, current)

       true
        } else false
    }
//    fun getLeft(num:Int):Float{
//        when(num){
//            1 ->
//        }
//    }
@Throws(IOException::class)
fun saveMyBitmap(bmp: Bitmap, bitName: String): Boolean {
    val dirFile = File("./sdcard/DCIM/Camera/")
    if (!dirFile.exists()) {
        dirFile.mkdirs()
    }
    val f = File(FileUtil.getResourceHome()+"$bitName.png")
    var flag = false
    f.createNewFile()
    var fOut: FileOutputStream? = null
    try {
        fOut = FileOutputStream(f)
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        flag = true
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }

    try {
        fOut!!.flush()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    try {
        fOut!!.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return flag
}

}