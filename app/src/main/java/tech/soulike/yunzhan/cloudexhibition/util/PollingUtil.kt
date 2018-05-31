package tech.soulike.yunzhan.cloudexhibition.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

/**
 * Created by thunder on 18-3-6.
 *
 */
object PollingUtil {
    @SuppressLint("ShortAlarm")
//开启轮询服务
    fun startPollingService(context: Context, seconds: Int, cls: Class<*>, action: String) {
        //获取AlarmManager系统服务
        val manager = context
                .getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //包装需要执行Service的Intent
        val intent = Intent(context, cls)
        intent.action = action
        val pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //触发服务的起始时间
        val triggerAtTime = SystemClock.elapsedRealtime()

        //使用AlarmManger的setInexactRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
                seconds*1000.toLong(), pendingIntent)

    }

    //停止轮询服务
    fun stopPollingService(context: Context, cls: Class<*>, action: String) {
        val manager = context
                .getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, cls)
        intent.action = action
        val pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        //取消正在执行的服务
        manager.cancel(pendingIntent)
    }
}