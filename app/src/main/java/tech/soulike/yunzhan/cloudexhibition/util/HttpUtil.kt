package tech.soulike.yunzhan.cloudexhibition.util

import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.util.HashMap

/**
 * Created by thunder on 18-3-6.
 */
object HttpUtil {
    private val TAG = this.javaClass.name
    private val JSON = MediaType.parse("application/json; charset=utf-8")
    private val gson = Gson()
    private val okHttpClient = OkHttpClient()

    @Throws(IOException::class)
    private fun _get(url: String): Response {
        val request = Request.Builder().url(url).build()
        return this.okHttpClient.newCall(request).execute()
    }

    private fun _getBook(url: String, paramCallback: Callback) {
        val request = Request.Builder().url(url).build()
        this.okHttpClient.newCall(request).enqueue(paramCallback)
    }

    @Throws(IOException::class)
    private fun _post(paramString: String, vararg paramVarArgs: Param<Any>): Response {
        CacheUtil.getCookie()
        val requestBody = RequestBody.create(JSON, paramsTOJson(*paramVarArgs))
        val request = Request.Builder().url(paramString).post(requestBody).build()
        return OkHttpClient().newCall(request).execute()
    }

    private fun _postAsyn(flag: Int, url: String, paramCallback: Callback, vararg paramVarArgs: Param<Any>) {
        if (flag == 1) {
            val str = CacheUtil.getCookie()
            Log.d(TAG, "_postAsyn: " + str)
            val requestBody = RequestBody.create(JSON, paramsTOJson(*paramVarArgs))
            val request = Request.Builder().url(url).addHeader("cookie", str).post(requestBody).build()
            this.okHttpClient.newCall(request).enqueue(paramCallback)
        } else {
            val requestBody = RequestBody.create(JSON, paramsTOJson(*paramVarArgs))
            val request = Request.Builder().url(url).post(requestBody).build()
            this.okHttpClient.newCall(request).enqueue(paramCallback)
        }

    }


    @Throws(IOException::class)
    operator fun get(paramString: String): Response {
        return _get(paramString)
    }

    private fun _getAsny(url: String, callback: Callback) {
        val request = Request.Builder()
                .url(url)
                .build()
        okHttpClient.newCall(request).enqueue(callback)
    }

    fun getAsny(url: String, callback: Callback) {
        _getAsny(url, callback)
    }
    private fun  paramsTOJson(vararg paramVarArgs: Param<Any>): String {
        val localHashMap = HashMap<String,Any>()
        paramVarArgs.forEach {
            localHashMap.put(it.key,it.value)
        }
        val json = gson.toJson(localHashMap)
        Log.d(TAG, "paramsTOJson: " + json)
        return json
    }

    @Throws(IOException::class)
    fun<T:Any> post(paramString: String, vararg paramVarArgs: Param<T>): Response {
        return _post(paramString, *paramVarArgs)
    }

    fun postAsyn(paramString: String, paramCallback: Callback, vararg paramVarArgs: Param<Any>) {
        _postAsyn(0, paramString, paramCallback, *paramVarArgs)
    }

    fun postAsynWithCookie(paramString: String, paramCallback: Callback, vararg paramVarArgs: Param<Any>) {
        _postAsyn(1, paramString, paramCallback, *paramVarArgs)
    }

    class Param<out T>(val key: String, val value:T)
}