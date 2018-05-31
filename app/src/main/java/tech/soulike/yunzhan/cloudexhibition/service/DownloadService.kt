package tech.soulike.yunzhan.cloudexhibition.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import tech.soulike.yunzhan.cloudexhibition.listener.DownloadListener
import tech.soulike.yunzhan.cloudexhibition.util.LogUtil

class DownloadService : Service() {
    var downloadTask: DownloadTask? = null
    private val downloadURL: String? = null
    private var downloadURLS: List<String>? = null
    fun setPause(isPause: Boolean) {
        if (downloadTask != null) {
            downloadTask!!.setPause(isPause)
        }

    }
    private var downloadListener: DownloadListener? = null
    private val downloadBinder = DownloadBinder()

    fun setDownloadListener(downloadListener: DownloadListener) {
        this.downloadListener = downloadListener
    }

    fun setDownloadURLS(downloadURLS: List<String>) {
        this.downloadURLS = downloadURLS
        downloadURLS.forEach {
            LogUtil.d("url",it)
        }
    }

    override fun onBind(intent: Intent): IBinder?  = downloadBinder

    inner class DownloadBinder : Binder() {
        val service: DownloadService
            get() = this@DownloadService
        fun startDownload() {
            if (!downloadURLS!!.isEmpty()){
                downloadTask = DownloadTask(downloadListener, downloadURLS)
                downloadTask?.execute(downloadURLS?.size) ?: return
            }

        }


    }
}
