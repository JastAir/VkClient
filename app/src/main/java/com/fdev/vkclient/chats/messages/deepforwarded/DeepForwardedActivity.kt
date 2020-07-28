package com.fdev.vkclient.chats.messages.deepforwarded

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.activities.ContentActivity

class DeepForwardedActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = DeepForwardedFragment.newInstance(intent?.extras)

    companion object {
        fun launch(context: Context?, messageId: Int) {
            context?.startActivity(Intent(context, DeepForwardedActivity::class.java).apply {
                putExtra(DeepForwardedFragment.ARG_MESSAGE_ID, messageId)
            })
        }
    }
}