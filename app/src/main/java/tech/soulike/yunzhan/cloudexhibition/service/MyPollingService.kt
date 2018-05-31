package tech.soulike.yunzhan.cloudexhibition.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import org.litepal.crud.DataSupport
import tech.soulike.yunzhan.cloudexhibition.base.JsonData
import tech.soulike.yunzhan.cloudexhibition.base.MD5
import tech.soulike.yunzhan.cloudexhibition.base.ResourceController
import tech.soulike.yunzhan.cloudexhibition.base.ResourceData
import tech.soulike.yunzhan.cloudexhibition.util.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.ArrayList

class MyPollingService : Service() {

    private var downloadTask: DownloadTask? = null
    private lateinit var pollingData: JsonData

    companion object {
        const val ACTION = "tech.soulike.yunzhan.cloudexhibition.service.PollingService"
    }

    var needUpdate = false
    private val downloadListener = NewDownloadListener()
    private val resourceUrl: String? = null
    private var jsonUrl: String? = null


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStart(intent: Intent, startId: Int) {
        PollingThread().start()
    }

    private fun needUpdate(mD5: String): Boolean {
        if (!CacheUtil.getScreenMD5().equals(mD5, true)) {
            return true
        }
        val resourceList: List<ResourceData> = DataSupport.findAll(ResourceData::class.java)
        val files = File(FileUtil.getResourceHome() + "resource/").listFiles()
        if (files.isEmpty() && !resourceList.isEmpty()) {
            return true
        }
        if (resourceList.isEmpty()) {
            return false
        }
        for (resourceDate in resourceList) {
            var needUpdate = true
            for (file in files) {
                if (resourceDate.adMd5.equals(MD5.getFileMD5String(file), true)) {
                    needUpdate = false
                    break
                }
            }
            if (needUpdate) {
                return true
            }
        }

        return false

    }

    internal inner class PollingThread : Thread() {
        override fun run() {
            super.run()
            try {
                val r = HttpUtil.post(StringUtil.URL + "poll", HttpUtil.Param("uuid", CacheUtil.getScreenId()))
                val content = r.body().string()
                Log.d("content ====", "run: $content")
                pollingData = Gson().fromJson<JsonData>(content, JsonData::class.java)

                CacheUtil.setPullTime(Integer.valueOf(pollingData.data.time))
                if (needUpdate(pollingData.data.md5!!)) {
                    val response = HttpUtil.post(StringUtil.URL + "get_json", HttpUtil.Param("uuid", CacheUtil.getScreenId()))
                    val jsonContent = response.body().string()
                    var screenInfo: JsonData? = null
                    try {
                        screenInfo = Gson().fromJson<JsonData>(jsonContent, JsonData::class.java)
                    } catch (e: Exception) {
                        val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVER")
                        intent.putExtra("type", 2)
                        sendBroadcast(intent)
                    }

                    if (screenInfo == null) {
                        val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVER")
                        intent.putExtra("type", 2)
                        sendBroadcast(intent)
                    } else {
                        val stringList = ArrayList<String>()
                        stringList.add(screenInfo.data.json_url)
                        jsonUrl = screenInfo.data.json_url
                        downloadTask = DownloadTask(downloadListener, stringList)
                        downloadTask!!.execute(stringList.size)
                    }

                }

            } catch (e: IOException) {
                e.printStackTrace()
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Service:onDestroy")
    }

    inner class NewDownloadListener : tech.soulike.yunzhan.cloudexhibition.listener.DownloadListener {

        override fun onProgress(vararg progresses: Int) {

        }

        override fun onSuccess(type: Int) {
            if (type == 1) {
                Thread(Runnable {
                    try {
                        val fileInputStream = FileInputStream(File(CacheUtil.getJSONPostion()))
                        val bytes = ByteArray(1024)
                        var b = 0
                        val result = StringBuilder()
                        b = fileInputStream.read(bytes)
                        try {
                            while (b > 0) {
                                result.append(String(bytes, 0, b))
                                b = fileInputStream.read(bytes)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val jsonData: JsonData
                        jsonData = Gson().fromJson<JsonData>(
                                result.toString(),
                                JsonData::class.java
                        )
                        var pictureList = arrayListOf<String>()
                        val originMap = hashMapOf<String, String>()
                        var resourceData: ResourceData
                        DataSupport.deleteAll(ResourceData::class.java)
                        jsonData.ad.forEach {
                            resourceData = ResourceData()
                            resourceData.adId = it.ad_id
                            resourceData.adMd5 = it.ad_md5.toLowerCase()
                            resourceData.adName = it.ad_name
                            resourceData.adTime = it.ad_time
                            resourceData.adUrl = it.ad_url
                            resourceData.qrCodeUpdate = it.ad_qrcode_update
                            resourceData.qrCodePosition = it.ad_qrcode_position
                            resourceData.qrCodeUpdate = it.ad_qrcode_update
                            resourceData.save()
                            pictureList.add(it.ad_url)
                            originMap[it.ad_url] = it.ad_md5
                        }
                        pictureList = ResourceController.setPicture(originMap)
                        val resourceDataList = arrayListOf<Int>()
                        jsonData.ad.filter {
                            it.ad_qrcode_update == 0
                        }.forEach {
                            resourceDataList.add(it.ad_id)
                        }
                        resourceDataList.forEach {
                            getQrCode(it, pictureList)
                        }
                        val downloadTask = DownloadTask(downloadListener, pictureList)
                        downloadTask.execute(pictureList.size)


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }).start()


            } else {
                FileUtil.deleteFile(File(CacheUtil.getJSONPostion()))
                CacheUtil.setScreenMD5(pollingData.data.md5!!)
                val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVE")
                intent.putExtra("type", 1)
                sendBroadcast(intent)
            }
        }

        override fun onFailed() {
            val intent = Intent("tech.soulike.yunzhan.cloudexhibition.RECEIVER")
            intent.putExtra("type", 2)
            sendBroadcast(intent)
        }

        override fun after() {

        }


        private fun getQrCode(id: Int, list: MutableList<String>) {
            val response = HttpUtil.post(StringUtil.URL + "get_qrcode", HttpUtil.Param("uuid", CacheUtil.getScreenId()), HttpUtil.Param("ad_id", id))
            val content = response.body().string()
            val qrCodeData = Gson().fromJson(content, JsonData::class.java)
            list.add(qrCodeData.data.qr_url + ">>" + id)
        }
    }
}
