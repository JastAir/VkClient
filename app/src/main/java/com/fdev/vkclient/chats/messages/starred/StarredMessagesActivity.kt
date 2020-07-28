package com.fdev.vkclient.chats.messages.starred

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.utils.launchActivity

class StarredMessagesActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = StarredMessagesFragment.newInstance()

    companion object {

        fun launch(context: Context?) {
            launchActivity(context, StarredMessagesActivity::class.java)
        }
    }
}