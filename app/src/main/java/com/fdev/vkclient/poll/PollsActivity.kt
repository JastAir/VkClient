package com.fdev.vkclient.poll

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.model.attachments.Poll

class PollsActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = PollFragment.newInstance(intent?.extras)

//    override fun getNavigationBarColor() = Color.TRANSPARENT

    companion object {

        fun launch(context: Context?, poll: Poll) {
            context?.startActivity(Intent(context, PollsActivity::class.java).apply {
                putExtras(PollFragment.getArgsLighter(poll))
            })
        }
    }

}