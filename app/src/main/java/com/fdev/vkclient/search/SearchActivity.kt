package com.fdev.vkclient.search

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.R
import com.fdev.vkclient.activities.ContentActivity

class SearchActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?) = SearchFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            context?.startActivity(Intent(context, SearchActivity::class.java))
        }
    }
}