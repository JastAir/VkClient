package com.fdev.vkclient.features

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.utils.launchActivity

class FeaturesActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = FeaturesFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, FeaturesActivity::class.java)
        }
    }
}