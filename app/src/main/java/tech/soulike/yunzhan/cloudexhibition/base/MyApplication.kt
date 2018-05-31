package tech.soulike.yunzhan.cloudexhibition.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import org.litepal.LitePal
import tech.soulike.yunzhan.cloudexhibition.util.SharedPreferenceUtil

/**
 * Created by thunder on 18-3-6.
 *
 */
class MyApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    private fun initPrefs() {
        SharedPreferenceUtil.init(context!!, packageName + "_preference")
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        initPrefs()
        LitePal.initialize(this)
    }
}