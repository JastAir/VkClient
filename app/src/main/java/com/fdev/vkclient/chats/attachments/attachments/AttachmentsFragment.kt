package com.fdev.vkclient.chats.attachments.attachments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.fdev.vkclient.R
import com.fdev.vkclient.adapters.BasePagerAdapter
import com.fdev.vkclient.base.BaseFragment
import com.fdev.vkclient.chats.attachments.audios.AudioAttachmentsFragment
import com.fdev.vkclient.chats.attachments.docs.DocAttachmentsFragment
import com.fdev.vkclient.chats.attachments.links.LinkAttachmentsFragment
import com.fdev.vkclient.chats.attachments.photos.PhotoAttachmentsFragment
import com.fdev.vkclient.chats.attachments.videos.VideoAttachmentsFragment
import com.fdev.vkclient.utils.stylize
import kotlinx.android.synthetic.main.fragment_attachments_history.*

class AttachmentsFragment : BaseFragment() {

    private val adapter by lazy {
        BasePagerAdapter(childFragmentManager)
    }

    private val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }

    override fun getLayoutId() = R.layout.fragment_attachments_history

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.attachments))
    }

    private fun initAdapter() {
        with(adapter) {
            add(PhotoAttachmentsFragment.newInstance(peerId), getString(R.string.photos))
            add(AudioAttachmentsFragment.newInstance(peerId), getString(R.string.audios))
            add(VideoAttachmentsFragment.newInstance(peerId), getString(R.string.videos))
            add(LinkAttachmentsFragment.newInstance(peerId), getString(R.string.links))
            add(DocAttachmentsFragment.newInstance(peerId), getString(R.string.docs))
        }
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager, true)
        tabs.stylize()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    companion object {
        const val ARG_PEER_ID = "peerId"

        fun newInstance(peerId: Int): AttachmentsFragment {
            val frag = AttachmentsFragment()
            frag.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return frag
        }
    }

}