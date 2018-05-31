package tech.soulike.yunzhan.cloudexhibition.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.io.File

/**
 * Created by thunder on 18-3-5.
 *
 */
inline fun<reified T: Activity> Context.startActivity()= startActivity(Intent(this,T::class.java))
