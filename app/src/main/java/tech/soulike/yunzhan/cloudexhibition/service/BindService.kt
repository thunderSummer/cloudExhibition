package tech.soulike.yunzhan.cloudexhibition.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.widget.Toast
import com.google.gson.Gson
import tech.soulike.yunzhan.cloudexhibition.base.JsonData
import tech.soulike.yunzhan.cloudexhibition.util.*
import java.io.IOException

class BindService : Service() {
    companion object {
        const val  ACTION = "tech.soulike.yunzhan.cloudexhibition.service.BindService"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStart(intent: Intent?, startId: Int) {
        checkBind()
    }
    /**
     * Used to ask the server whether to complete the binding screen
     * This method will be run automatically when this app starts up
     */
    private fun checkBind(){
        Thread{
            try {
                val response = HttpUtil.post("${StringUtil.URL}check_bind",HttpUtil.Param("uuid",CacheUtil.getScreenId()))
                val content = response.body().string()
                LogUtil.d("sssss",content)
                val jsonData: JsonData = Gson().fromJson(content,JsonData::class.java)
                jsonData.let {
                    val result = Intent("${StringUtil.PACKAGE_NAME}.activity.MainActivity")
                    result.putExtra(StringUtil.CHECK_RESULT,it.data.is_user)
                    sendBroadcast(result)
                }} catch (e: IOException) {
                val result1 = Intent("${StringUtil.PACKAGE_NAME}.activity.MainActivity")
                result1.putExtra(StringUtil.CHECK_RESULT, 3)
                sendBroadcast(result1)
            }
        }.start()



    }
}
