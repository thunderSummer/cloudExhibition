package tech.soulike.yunzhan.cloudexhibition.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("StaticFieldLeak")
/**
 * Created by thunder on 18-3-6.
 *
 */
object SharedPreferenceUtil {
    lateinit var context: Context
    lateinit var editor: SharedPreferences.Editor
    lateinit var prefs: SharedPreferences
    @SuppressLint("CommitPrefEdits")
    fun init(paramContext: Context, s: String) {
        context = paramContext
        prefs = context.getSharedPreferences(s, 0)
        editor =prefs.edit()
    }

    fun getLong(s: String): Long {
        return this.prefs.getLong(s, 0)
    }

    fun getBoolean(s: String): Boolean {
        return this.prefs.getBoolean(s, false)
    }

    fun getBoolean(s: String, b: Boolean): Boolean {
        return this.prefs.getBoolean(s, b)
    }

    fun getInt(s: String, i: Int): Int {
        return this.prefs.getInt(s, i)
    }

    fun getString(s: String): String {
        return this.prefs.getString(s, "")
    }

    fun getString(s1: String, s2: String): String {
        return this.prefs.getString(s1, s2)
    }

    fun putBoolean(s: String, b: Boolean) {
        this.editor.putBoolean(s, b)
        this.editor.apply()
    }

    fun putLong(s: String, l: Long) {
        this.editor.putLong(s, l)
        this.editor.apply()
    }

    fun putInt(s: String, paramInt: Int): SharedPreferenceUtil {
        this.editor.putInt(s, paramInt)
        this.editor.commit()
        return this
    }

    fun putString(s1: String, s2: String): SharedPreferenceUtil {
        this.editor.putString(s1, s2)
        this.editor.commit()
        return this
    }

    fun remove(s: String): SharedPreferenceUtil {
        this.editor.remove(s)
        this.editor.commit()
        return this
    }

    fun removeAll(): SharedPreferenceUtil {
        this.editor.clear()
        this.editor.commit()
        return this
    }
}