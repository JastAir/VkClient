package com.fdev.vkclient.wallpost

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.R
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.utils.launchActivity

class WallPostActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?) = WallPostFragment.newInstance(
            intent?.extras?.getString(WallPostFragment.ARG_POST_ID) ?: ""
    )

    companion object {
        fun launch(context: Context?, postId: String) {
            launchActivity(context, WallPostActivity::class.java) {
                putExtra(WallPostFragment.ARG_POST_ID, postId)
            }
        }
    }
}