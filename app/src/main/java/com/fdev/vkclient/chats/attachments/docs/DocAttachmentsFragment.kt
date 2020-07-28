package com.fdev.vkclient.chats.attachments.docs

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.App
import com.fdev.vkclient.chats.attachments.base.BaseAttachmentsFragment
import com.fdev.vkclient.model.attachments.Doc
import com.fdev.vkclient.utils.simpleUrlIntent
import com.fdev.vkclient.web.GifViewerActivity

class DocAttachmentsFragment : BaseAttachmentsFragment<Doc>() {

    override val adapter by lazy {
        DocAttachmentsAdapter(contextOrThrow, ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(doc: Doc) {
        if (doc.isGif) {
            GifViewerActivity.showGif(context, doc)
        } else {
            simpleUrlIntent(context, doc.url)
        }
    }

    override fun getViewModelClass() = DocAttachmentsViewModel::class.java

    companion object {
        fun newInstance(peerId: Int): DocAttachmentsFragment {
            val fragment = DocAttachmentsFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}