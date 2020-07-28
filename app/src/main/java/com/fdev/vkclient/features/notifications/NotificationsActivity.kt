package com.fdev.vkclient.features.notifications

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.utils.launchActivity

class NotificationsActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = NotificationsFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, NotificationsActivity::class.java)
        }
    }
}