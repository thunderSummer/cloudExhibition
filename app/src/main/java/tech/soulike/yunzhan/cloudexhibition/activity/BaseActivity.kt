package tech.soulike.yunzhan.cloudexhibition.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import tech.soulike.yunzhan.cloudexhibition.util.LogUtil

@SuppressLint("Registered")
/**
 * Created by thunder on 18-3-5.
 *
 */
open class BaseActivity : AppCompatActivity() {
    protected val TAG = javaClass.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
        LogUtil.d(TAG, "-------->Create")

    }
}