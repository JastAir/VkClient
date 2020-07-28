package com.fdev.vkclient.chats.attachments.links

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.App
import com.fdev.vkclient.chats.attachments.base.BaseAttachmentsFragment
import com.fdev.vkclient.model.attachments.Link
import com.fdev.vkclient.utils.simpleUrlIntent

class LinkAttachmentsFragment : BaseAttachmentsFragment<Link>() {

    override val adapter by lazy {
        LinkAttachmentsAdapter(contextOrThrow, ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun getViewModelClass() = LinkAttachmentsViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(link: Link) {
        simpleUrlIntent(context, link.url)
    }

    companion object {
        fun newInstance(peerId: Int): LinkAttachmentsFragment {
            val fragment = LinkAttachmentsFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}
