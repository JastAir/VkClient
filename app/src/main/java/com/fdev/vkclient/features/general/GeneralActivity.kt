package com.fdev.vkclient.features.general

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.utils.launchActivity

class GeneralActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = GeneralFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, GeneralActivity::class.java)
        }
    }
}