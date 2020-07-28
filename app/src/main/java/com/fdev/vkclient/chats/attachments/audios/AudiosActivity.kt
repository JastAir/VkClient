package com.fdev.vkclient.chats.attachments.audios

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.R
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.managers.Session

class AudiosActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?) = AudioAttachmentsFragment.newInstance(Session.uid)

    companion object {
        fun launch(context: Context?) {
            context?.startActivity(Intent(context, AudiosActivity::class.java))
        }
    }
}