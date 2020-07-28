package com.fdev.vkclient.chats.attachments.attachments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.fdev.vkclient.R
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.main.InsetViewModel
import com.fdev.vkclient.utils.consumeInsets
import kotlinx.android.synthetic.main.activity_content.*

class AttachmentsActivity : ContentActivity() {

    private val insetViewModel by lazy {
        ViewModelProviders.of(this)[InsetViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.activity_content

    override fun createFragment(intent: Intent?)
            = AttachmentsFragment.newInstance(intent?.extras?.getInt(ARG_PEER_ID) ?: 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vShadow.consumeInsets { top, bottom ->
            insetViewModel.updateTopInset(top)
            insetViewModel.updateBottomInset(bottom)
        }
    }

    companion object {

        const val ARG_PEER_ID = "peerId"

        fun launch(context: Context?, peerId: Int) {
            if (context == null) return

            context.startActivity(Intent(context, AttachmentsActivity::class.java).apply {
                putExtra(ARG_PEER_ID, peerId)
            })
        }
    }
}