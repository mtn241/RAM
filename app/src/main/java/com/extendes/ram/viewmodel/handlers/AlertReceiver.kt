package com.extendes.ram.viewmodel.handlers


import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.extendes.ram.App
import com.extendes.ram.R
import com.extendes.ram.view.ui.MainActivity

class AlertReceiver:BroadcastReceiver() {
    private val notificationManager=NotificationManagerCompat.from(App.instance)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if(intent?.action== App.ACTION_ON_TASK_ALARM){
                val toMain=Intent(context, MainActivity::class.java)
                toMain.putExtra("task_id", intent.dataString?.toLong())
                toMain.action= App.ACTION_NOTIFICATION_CLICK
                val pending=PendingIntent.getActivity(context,2,toMain,PendingIntent.FLAG_UPDATE_CURRENT)
                val notification=NotificationCompat.Builder(context, App.CHANNEL_1_ID).
                setSmallIcon(R.drawable.task_list_item).
                setContentIntent(pending).
                setPriority(NotificationCompat.PRIORITY_DEFAULT).
                setColor(intent.extras!!.getInt("color", R.color.design_default_color_error)).
                    setStyle(NotificationCompat.BigTextStyle()
                        .bigText(intent.extras?.getString("message"))).
                setContentTitle(context.resources.getString(R.string.alarm_notification_title)).setContentText(intent.extras?.getString("message")).setDefaults(Notification.DEFAULT_SOUND).build()
                notificationManager.notify(1,notification)
            }
            /*
            else if(intent?.action==App.ACTION_ON_REPEAT_UPDATE){
            }
             */
        }

    }

}