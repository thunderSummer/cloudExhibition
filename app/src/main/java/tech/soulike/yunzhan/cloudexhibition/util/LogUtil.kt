package tech.soulike.yunzhan.cloudexhibition.util

import android.util.Log

/**
 * Created by thunder on 18-3-5.
 *
 */
object LogUtil {
    val DEBUG = 2
    val ERROR = 5
    val INFO = 3
    val NOTHING = 6
    val VERBOSE = 1
    val WARN = 4
    var level = 3

    fun d(tag: String, msg: String) {
        if (level <= 2) {
            Log.d(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (level <= 5) {
            Log.e(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (level <= 3) {
            Log.i(tag, msg)
        }
    }

    fun v(tag: String, msg: String) {
        if (level <= 1) {
            Log.v(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (level <= 4) {
            Log.w(tag, msg)
        }
    }
}