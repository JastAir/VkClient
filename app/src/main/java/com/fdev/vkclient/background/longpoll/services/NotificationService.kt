package com.fdev.vkclient.background.longpoll.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fdev.vkclient.background.longpoll.LongPollCore


class NotificationService : Service() {

    companion object {

        fun launch(context: Context, intent: Intent = Intent(context, NotificationService::class.java)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context?, intent: Intent = Intent(context, NotificationService::class.java)) {
            context?.stopService(intent)
        }
    }

    private val core by lazy { LongPollCore(applicationContext) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            core.run(intent)
        }.start()
        core.showForeground(this)
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        core.onDestroy()
        super.onDestroy()
        launch(applicationContext)
    }

    override fun onBind(intent: Intent?) = null
}
