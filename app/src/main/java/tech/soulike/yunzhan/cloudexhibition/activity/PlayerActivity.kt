package tech.soulike.yunzhan.cloudexhibition.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import tech.soulike.yunzhan.cloudexhibition.R
import tech.soulike.yunzhan.cloudexhibition.listener.ImageChangeListener
import tech.soulike.yunzhan.cloudexhibition.util.PollingUtil
import tech.soulike.yunzhan.cloudexhibition.view.BaseImage
import tech.soulike.yunzhan.cloudexhibition.service.MyPollingService
import tech.soulike.yunzhan.cloudexhibition.util.LogUtil


class PlayerActivity : AppCompatActivity() {
    private lateinit var baseImage: BaseImage;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val broadcastReceiver = MyReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("tech.soulike.yunzhan.cloudexhibition.RECEIVE")
        registerReceiver(broadcastReceiver, intentFilter)
        baseImage = BaseImage(this@PlayerActivity)
        baseImage.setImageChangeListener(object : ImageChangeListener{
            override fun onBitmapChange(currentId: Int) {

            }

            override fun onBitmapPause(){
                baseImage.visibility=View.INVISIBLE
                /**
                 * 播放视频
                 */


            }

        })
        setContentView(R.layout.activity_player)
        val frameLayout = findViewById<View>(R.id.container) as FrameLayout
        frameLayout.addView(baseImage)
        PollingUtil.startPollingService(this, 10, MyPollingService::class.java, MyPollingService.ACTION)
    }

    private inner class MyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.getIntExtra("type", 0)
            if (type == 1) {
                Toast.makeText(this@PlayerActivity, "正在更新资源包", Toast.LENGTH_LONG).show()
                baseImage.restart()
            } else if (type == 2) {
                Toast.makeText(this@PlayerActivity, "资源包验证失败", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@PlayerActivity, HelloActivity::class.java))
                finish()
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        PollingUtil.stopPollingService(this, MyPollingService::class.java, MyPollingService.ACTION)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}
