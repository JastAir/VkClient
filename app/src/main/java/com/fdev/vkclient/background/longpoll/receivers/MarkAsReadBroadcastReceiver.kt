package com.fdev.vkclient.background.longpoll.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fdev.vkclient.App
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.utils.ApiUtils
import javax.inject.Inject

class MarkAsReadBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var apiUtils: ApiUtils

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        try {
            App.appComponent?.inject(this)
            val action = intent.action ?: return
            val messageId = intent.extras?.getInt(ARG_MESSAGE_ID) ?: return
            val peerId = intent.extras?.getInt(ARG_PEER_ID) ?: return

            Lg.i("[markAsRead] received $action with message $messageId for peer $peerId")

            if (action == ACTION_MARK_AS_READ) {
                apiUtils.markAsRead("$messageId")
                (context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
                        ?.cancel(peerId)
            }
        } catch (e: Exception) {
            Lg.wtf("[markAsRead] unable to mark message: ${e.message}\ndata: ${intent.extras}")
        }
    }

    companion object {

        const val ACTION_MARK_AS_READ = "markAsReadAction"
        const val ARG_MESSAGE_ID = "messageId"
        const val ARG_PEER_ID = "peerId"
    }
}