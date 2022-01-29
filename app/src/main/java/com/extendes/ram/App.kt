package com.extendes.ram

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.os.Build


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        createAppNotificationChannels()
        instance = this

    }
    private fun createAppNotificationChannels(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channelOne = NotificationChannel(CHANNEL_1_ID, NAME_1,NotificationManager.IMPORTANCE_DEFAULT)
            val uri = getDefaultUri(TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            channelOne.setSound(uri,audioAttributes)
            val manager=getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channelOne)
        }
    }
    companion object {
        const  val  CHANNEL_1_ID:String="Wake on task alarm"
        const val ACTION_NOTIFICATION_CLICK:String="ACTION_NOTIFICATION_CLICK"
        const val ACTION_ON_TASK_ALARM:String="ACTION_ON_TASK_ALARM"
        //const val ACTION_ON_REPEAT_UPDATE:String="ACTION_ON_REPEAT_UPDATE" //TODO task repeat not implemented yet
        //const val ACTION_RUN_UPDATE_SERVICE:String="ACTION_RUN_UPDATE_SERVICE" //TODO task repeat not implemented yet
        const  val  NAME_1:String="Awake on task"
        lateinit var instance: App
    }
}