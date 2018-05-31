package tech.soulike.yunzhan.cloudexhibition.service

import android.os.AsyncTask
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import tech.soulike.yunzhan.cloudexhibition.base.MD5
import tech.soulike.yunzhan.cloudexhibition.listener.DownloadListener
import tech.soulike.yunzhan.cloudexhibition.util.CacheUtil
import tech.soulike.yunzhan.cloudexhibition.util.FileUtil
import tech.soulike.yunzhan.cloudexhibition.util.HttpUtil
import tech.soulike.yunzhan.cloudexhibition.util.LogUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.SocketTimeoutException

/**
 * Created by thunder on 18-3-6.
 *
 */
class DownloadTask(private var downloadListener: DownloadListener?, private var downloadUrls: List<String>?) : AsyncTask<Int, Int, Int>() {
    private var downloadType = 0
    private var downloadSize: Int = 0
    private val isStart = false
    private var isPause: Boolean? = false


    private var lastProgress: Int = 0

    fun setPause(pause: Boolean?) {
        isPause = pause
    }

    fun setDownloadUrls(downloadUrls: List<String>) {
        this.downloadUrls = downloadUrls
    }

    override fun doInBackground(vararg integers: Int?): Int {
        var inputStream: InputStream? = null
        var randomAccessFile: RandomAccessFile? = null
        var i = 0
        var file: File? = null
        var failNum = 0
        val downloadLength: Long = 0
        downloadSize = integers[0]!!
        val file1 = File(FileUtil.getResourceHome() + "resource/")
        val file2 = File(FileUtil.getResourceHome()+"qrcode/")
        val rootFile = File(FileUtil.getResourceHome())
        if (!rootFile.exists()) {
            rootFile.mkdir()
        }
        if (!file1.exists()) {
            file1.mkdir()
        }
        if (!file2.exists()){
            file2.mkdir()
        }
        while (i < downloadSize) {
            var downloadURL = downloadUrls!![i]
            var adId = -1
            if (downloadURL.contains(">>")){
                adId = downloadURL.substring(downloadURL.lastIndexOf(">")+1).toInt()
                downloadURL= downloadURL.substring(0,downloadURL.indexOf(">"))
            }
            val fileName = downloadURL.substring(downloadURL.lastIndexOf("/"))
            when {
                adId!=-1 -> file = File(FileUtil.getResourceHome() + "qrcode/qrcode_" + adId)
                fileName.contains(".json") -> {
                    file = File(FileUtil.getResourceHome() + fileName)
                    downloadType = 1
                }
                else -> {
                    file = File(FileUtil.getResourceHome() + "resource/" + fileName)
                    downloadType = 0
                }
            }

//            if (fileName.contains(".json")) {
//                try {
//                    if (CacheUtil.getScreenMD5().equals(MD5.getFileMD5String(file),true)) {
//                        CacheUtil.setJOSNPosition(file.absolutePath)
//                        return TYPE_SUCCESS
//                    } else {
//                        CacheUtil.setJOSNPosition(file.absolutePath)
//                    }
//
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//
//            }

            try {
                val contentLength = getContentLength(downloadURL)
                if (contentLength == 0L) {
                    failNum++
                    i++
                    publishProgress(i, downloadSize)
                    continue
                } else if (contentLength == downloadLength) {
                    i++
                    publishProgress(i, downloadSize)
                    continue
                }
                Log.d("tag", "doInBackground: 正在下载$downloadURL 共 $downloadSize 文件")

                val okHttpClient = OkHttpClient()
                val request = Request.Builder()
                        .addHeader("RANGE", "bytes=$downloadLength-")
                        .url(downloadURL)
                        .build()
                val response = okHttpClient.newCall(request).execute()
                if (response != null) {
                    inputStream = response.body().byteStream()
                    randomAccessFile = RandomAccessFile(file, "rw")
                    randomAccessFile.seek(downloadLength)
                    val b = ByteArray(1024)
                    var total = 0
                    var len: Int = inputStream.read(b)
                    while (len!= -1) {
                        if (isPause!!) {
                            return TYPE_PAUSE
                        }
                        total += len
                        randomAccessFile.write(b, 0, len)
                        len = inputStream.read(b)
                    }
                }
                assert(response != null)
                response!!.body().close()
                i++
                if (fileName.contains(".json")){
                    CacheUtil.setJOSNPosition(file.absolutePath)
                }
                if (i == downloadSize) {
                    return if (failNum == 0) {
                        TYPE_SUCCESS
                    } else {
                        TYPE_FAILED
                    }
                }
                publishProgress(i, downloadSize)

            } catch (s: SocketTimeoutException) {
                s.printStackTrace()
                return TYPE_FAILED
            } catch (e: IOException) {
                e.printStackTrace()
                return TYPE_FAILED
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close()
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return TYPE_SUCCESS
    }

     override fun onProgressUpdate(vararg values: Int?) {
        val progress = values[0]
        val all = values[1]
        if (progress!! > lastProgress) {
            if (downloadListener != null) {
                downloadListener!!.onProgress(progress, all!!)
                lastProgress = progress
            }

        }
    }

    @Throws(IOException::class)
    private fun getContentLength(url: String): Long {
        val response = HttpUtil[url]
        return if (response.isSuccessful) {
            val contentLength = response.body().contentLength()
            response.close()
            contentLength
        } else {
            0
        }

    }

    override fun onPostExecute(integer: Int?) {
        when (integer) {
            TYPE_FAILED -> if (downloadListener != null) {
                downloadListener!!.onFailed()
                // downloadListener.after();
            }
            TYPE_SUCCESS -> if (downloadListener != null) {
                downloadListener!!.onSuccess(downloadType)
            }
            TYPE_PAUSE -> if (downloadListener != null) {
                downloadListener!!.after()
            }
            else -> {
            }
        }
    }
    companion object {
        const val TYPE_SUCCESS = 0
        const val TYPE_FAILED = 1
        const val TYPE_PAUSE = 2
    }
}