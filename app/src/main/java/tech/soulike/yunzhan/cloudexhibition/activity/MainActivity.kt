package tech.soulike.yunzhan.cloudexhibition.activity

import android.content.*
import android.content.pm.PackageInfo
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.litepal.crud.DataSupport
import tech.soulike.yunzhan.cloudexhibition.R
import tech.soulike.yunzhan.cloudexhibition.base.*
import tech.soulike.yunzhan.cloudexhibition.listener.DownloadListener
import tech.soulike.yunzhan.cloudexhibition.service.BindService
import tech.soulike.yunzhan.cloudexhibition.service.DownloadService
import tech.soulike.yunzhan.cloudexhibition.util.*
import tech.soulike.yunzhan.cloudexhibition.util.HttpUtil.postAsyn
import tech.soulike.yunzhan.cloudexhibition.view.RoundProgressBarWidthNumber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.ArrayList

class MainActivity : BaseActivity() {
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var UUID: TextView
    private lateinit var roundProgressBarWidthNumber: RoundProgressBarWidthNumber
    private lateinit var UUIDString: String
    private lateinit var downLoadService: DownloadService
    private lateinit var downloadBinder: DownloadService.DownloadBinder
    private lateinit var aSwitch: Switch
    private lateinit var autoTime: TextView
    private lateinit var packageInfo: PackageInfo
    private lateinit var jsonUrl: String
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var statusTV: TextView
    private lateinit var tempMD5: String
    private var busy = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (CacheUtil.getScreenId() == "") {
            val id = Installation.id(this)
            CacheUtil.saveScreenId(id.substring(0, 8))
        }
        UUIDString = CacheUtil.getScreenId()
        init()
        val bindIntent = Intent(this@MainActivity, DownloadService::class.java)
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE)
        if (!CacheUtil.isCreateScreen) {
            createScreen()

        }
        UUID.text = UUIDString
        checkBind()
    }

    /**
     * Used to ask the server whether to complete the binding screen
     * This method will be run automatically when this app starts up
     */
    private fun checkBind() {
        Thread {
            try {
                val response = HttpUtil.post("${StringUtil.URL}check_bind", HttpUtil.Param("uuid", UUIDString))
                val content = response.body().string()
                LogUtil.d("tag", content)
                val jsonData: JsonData = Gson().fromJson(content, JsonData::class.java)
                jsonData.let {
                    when (it.data.is_user) {
                        2 -> createScreen()
                        0 -> runOnUiThread {
                            Toast.makeText(this@MainActivity, "该屏幕尚未完成绑定", Toast.LENGTH_LONG).show()
                            val countDownTimer = object : CountDownTimer(60000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {}
                                override fun onFinish() {
                                    checkBind()
                                }
                            }
                            countDownTimer.start()
                        }
                        1 -> gainJson()
                    }
                }
            } catch (e: IOException) {
                runOnUiThread { Toast.makeText(this, "无法连接到服务器，请检查网络", Toast.LENGTH_LONG).show() }
                e.printStackTrace()
            }
        }.start()


    }

    /**
     * This method will be run Only once when this app starts up firstly
     */
    private fun createScreen() {
        Thread {
            try {
                runOnUiThread { Toast.makeText(this, "正在创建屏幕", Toast.LENGTH_SHORT).show() }
                val widthNumber = MyApplication.context.resources.displayMetrics.widthPixels
                val heightNumber = getHasVirtualKey()
                var screenResolution: String = heightNumber.toString() + " * " + widthNumber.toString()
                // this function should send location information
                val response = HttpUtil.post(StringUtil.URL + "create_screen", HttpUtil.Param("uuid", UUIDString), HttpUtil.Param("screen_resolution", screenResolution))
                Log.d(TAG, "createScreen: response:" + response.body().string())
                CacheUtil.saveScreen(true)
            } catch (e: IOException) {
                runOnUiThread { Toast.makeText(this, "无法连接到服务器，请检查网络", Toast.LENGTH_LONG).show() }
                e.printStackTrace()
            }
        }.start()


    }

    /**
     * Designed to get json, if no resources , app will exit after three seconds
     */
    private fun gainJson() {
        val response = HttpUtil.post("${StringUtil.URL}poll", HttpUtil.Param("uuid", UUIDString), HttpUtil.Param("id", 1))
        val content = response.body().string()
        val jsonData: JsonData
//        Log.d(TAG, "requestAll: onResponse: $content")
        jsonData = Gson().fromJson(content, JsonData::class.java)
        CacheUtil.setPullTime(Integer.parseInt(jsonData.data.time))
        if (jsonData.data.md5.isNullOrEmpty()) {
            statusTV.text = "无资源包"
            runOnUiThread{
                val countDownTimer = object : CountDownTimer(60000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        checkBind()
                    }
                }
                countDownTimer.start()
            }
        } else {
            if (needUpdate(jsonData.data.md5!!)) {
                try {
                    tempMD5 = jsonData.data.md5!!
                    val response1 = HttpUtil.post(StringUtil.URL + "get_json", HttpUtil.Param("uuid", UUIDString))
                    val responseContent = response1.body().string()
                    val result = Gson().fromJson<JsonData>(responseContent, JsonData::class.java)
                    runOnUiThread {
                        val list = ArrayList<String>()
                        jsonUrl = result.data.json_url
                        list.add(jsonUrl)
                        downLoadService.let {
                            it.setPause(false)
                            it.setDownloadURLS(list)
                        }
                        downloadBinder.startDownload()
                        roundProgressBarWidthNumber.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "无法连接服务器，获取资源文件失败", Toast.LENGTH_LONG).show()
                    }

                }
            } else {
                startActivity<PlayerActivity>()

            }

        }
//        runOnUiThread { roundProgressBarWidthNumber.visibility = View.VISIBLE }
    }

    /**
     * To get the Resource including picture , video
     */
    public fun gainResource() {
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
        jsonData.ad.forEach {
            resourceData = ResourceData()
            resourceData.adId = it.ad_id
            resourceData.adMd5 = it.ad_md5.toLowerCase()
            resourceData.adName = it.ad_name
            resourceData.adTime = it.ad_time
            resourceData.adUrl = it.ad_url
            resourceData.qrCodeUpdate = it.ad_qrcode_update
            resourceData.qrCodePosition = it.ad_qrcode_position
            resourceData.save()
            pictureList.add(it.ad_url)
            originMap[it.ad_url] = it.ad_md5
            SharedPreferenceUtil.putInt(it.ad_md5.toLowerCase(),it.ad_time)
        }
        pictureList = ResourceController.setPicture(originMap)
        runOnUiThread { roundProgressBarWidthNumber.visibility = View.VISIBLE }
        downLoadService.setPause(false)
        val resourceDataList = arrayListOf<Int>()
        jsonData.ad.filter {
            it.ad_qrcode_update == 0
        }.forEach {
            resourceDataList.add(it.ad_id)
        }
        LogUtil.d("ad num is ",""+pictureList.size)
        resourceDataList.forEach {
            getQrCode(it, pictureList)
        }
        downLoadService.setDownloadURLS(pictureList)
        downloadBinder.startDownload()
    }

    private var connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadBinder = service as DownloadService.DownloadBinder
            downLoadService = downloadBinder.service

            downLoadService.setDownloadListener(object : DownloadListener {
                override fun onProgress(vararg progress: Int) {
                    roundProgressBarWidthNumber.tipTitle = "正在获取资源文件"
                    roundProgressBarWidthNumber.setAllCount(progress[1])
                    roundProgressBarWidthNumber.setCurrent(progress[0])
                    roundProgressBarWidthNumber.progress = 100 * progress[0] / progress[1]
                }

                override fun onSuccess(type: Int) {
                    roundProgressBarWidthNumber.visibility = View.INVISIBLE
                    val content: String
                    if (type == 1) {
                        content = "获取资源目录成功"
                        Thread(Runnable {
                            try {
                                gainResource()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }).start()
                    } else {
                        content = "获取资源文件成功"
                        statusTV.text = "正常工作"
                        CacheUtil.setScreenMD5(tempMD5)
                        val file = File(CacheUtil.getJSONPostion())
                        FileUtil.deleteFile(file)
                        startActivity<PlayerActivity>()
                    }
                    Toast.makeText(this@MainActivity, content, Toast.LENGTH_LONG).show()
                }

                override fun onFailed() {
                    Toast.makeText(this@MainActivity, "资源文件获取失败，请重试", Toast.LENGTH_LONG).show()
                    roundProgressBarWidthNumber.visibility = View.INVISIBLE
                }

                override fun after() {
                    roundProgressBarWidthNumber.visibility = View.INVISIBLE
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

    private fun init() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_hello)
        toolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_main)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        autoTime = findViewById(R.id.auto_time_hello)
        autoTime.run {
            text = CacheUtil.getAutoTime()
            setOnClickListener {
                startActivity<ChooseTimeActivity>()
                drawerLayout.closeDrawer(Gravity.START)
            }
        }
        aSwitch = findViewById<Switch>(R.id.switch_hello)
        aSwitch.isChecked = CacheUtil.isAutoStart()
        aSwitch.setOnClickListener {
            if (aSwitch.isChecked) {
                CacheUtil.setAutoStart(true)
                Toast.makeText(this@MainActivity, "已设置开机自动启动", Toast.LENGTH_SHORT).show()
            } else {
                CacheUtil.setAutoStart(false)
                Toast.makeText(this@MainActivity, "已取消开机自动启动", Toast.LENGTH_SHORT).show()
            }
        }
        UUID = findViewById(R.id.id_hello)
        packageInfo = PackageInfo()
        roundProgressBarWidthNumber = findViewById(R.id.id_progress)
        val about = findViewById<TextView>(R.id.about_hello)
        val update = findViewById<TextView>(R.id.update_hello)
        about.setOnClickListener { Toast.makeText(this@MainActivity, "当前版本：V" + packageInfo.versionName, Toast.LENGTH_SHORT).show() }
//        update.setOnClickListener { checkUpdate() }
        statusTV = findViewById(R.id.status_main)

    }

    private fun checkUpdate() {
        Thread(Runnable {
            val versionCode = packageInfo.versionCode
            var response: Response? = null
            try {
                response = HttpUtil.post(StringUtil.URL + "request_update", HttpUtil.Param("uuid", UUIDString), HttpUtil.Param("version_code", versionCode))
                val content = response.body().string()
                val screenInfo = Gson().fromJson(content, JsonData::class.java)
                val url = screenInfo.data.url
                if (url == "") {
                    runOnUiThread { Toast.makeText(this@MainActivity, "目前已是最新版本", Toast.LENGTH_SHORT).show() }
                } else {
                    runOnUiThread {
                        showDialog(url)
                    }
                }
            } catch (e: IOException) {
                runOnUiThread { Toast.makeText(this@MainActivity, "无法连接到服务器，请检查网络", Toast.LENGTH_SHORT).show() }
                e.printStackTrace()
            }
        }).start()


    }

    private fun showDialog(url: String) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("检查到新版本")
        builder.setMessage("是否更新？")
        builder.setNegativeButton("取消", null)
        builder.setPositiveButton("确定") { _, _ ->
            Thread(Runnable {
                //                val intent = Intent(this@MainActivity, UpdateService::class.java)
//                intent.putExtra("downloadUrl", url)
//                startService(intent)
            }).start()
        }
        builder.show()
    }

//    internal inner class MainBroadcastReceive : BroadcastReceiver() {
//
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.getIntExtra(StringUtil.CHECK_RESULT, 3)) {
//                2 -> createScreen()
//                0 -> runOnUiThread {
//                    Toast.makeText(this@MainActivity, "该屏幕尚未完成绑定", Toast.LENGTH_LONG).show()
//                    statusTV.text = "屏幕未绑定"
//                }
//                1 -> {
//                    PollingUtil.stopPollingService(this@MainActivity, BindService::class.java, BindService.ACTION)
//                    gainJson()
//                }
//                3 -> runOnUiThread {
//                    Toast.makeText(this@MainActivity, "无法连接服务器", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }

    override fun onDestroy() {
        unbindService(connection)
        PollingUtil.stopPollingService(this@MainActivity, BindService::class.java, BindService.ACTION)
        super.onDestroy()
    }

    private fun getHasVirtualKey(): Int {
        var dpi = 0
        val display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        val c: Class<*>
        try {
            c = Class.forName("android.view.Display")
            val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            method.invoke(display, dm)
            dpi = dm.heightPixels
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dpi
    }

    private fun getQrCode(id: Int, list: MutableList<String>) {
        val response = HttpUtil.post(StringUtil.URL + "get_qrcode", HttpUtil.Param("uuid", UUIDString), HttpUtil.Param("ad_id", id))
        val content = response.body().string()
        val qrCodeData = Gson().fromJson(content, JsonData::class.java)
        list.add(qrCodeData.data.qr_url + ">>" + id)

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

}
