package com.fdev.vkclient.chats.attachments.attach

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.fdev.vkclient.R
import com.fdev.vkclient.adapters.BasePagerAdapter
import com.fdev.vkclient.base.BaseFragment
import com.fdev.vkclient.chats.attachments.docs.DocAttachFragment
import com.fdev.vkclient.chats.attachments.gallery.GalleryFragment
import com.fdev.vkclient.chats.attachments.gallery.model.DeviceItem
import com.fdev.vkclient.chats.attachments.photos.PhotoAttachFragment
import com.fdev.vkclient.chats.attachments.videos.VideoAttachFragment
import com.fdev.vkclient.model.attachments.Attachment
import com.fdev.vkclient.utils.stylize
import kotlinx.android.synthetic.main.fragment_attach.*

class AttachFragment : BaseFragment() {

    private val adapter by lazy {
        BasePagerAdapter(childFragmentManager)
    }

    override fun getLayoutId() = R.layout.fragment_attach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(adapter) {
            add(GalleryFragment.newInstance(onSelected = ::onSelectedFromGallery), getString(R.string.device_photos))
            add(PhotoAttachFragment.newInstance(::onAttachmentsSelected), getString(R.string.photos))
            add(VideoAttachFragment.newInstance(::onAttachmentsSelected), getString(R.string.videos))
            add(DocAttachFragment.newInstance(::onAttachmentsSelected), getString(R.string.docs))
            vpAttach.adapter = this
        }
        tabs.setupWithViewPager(vpAttach, true)
        tabs.stylize()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateTitle(getString(R.string.attach))
    }

    private fun onSelectedFromGallery(paths: List<DeviceItem>) {
        val intent = Intent().apply {
            putParcelableArrayListExtra(ARG_PATHS, ArrayList(paths))
        }
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    private fun onAttachmentsSelected(attachments: List<Attachment>) {
        val intent = Intent().apply {
            putParcelableArrayListExtra(ARG_ATTACHMENTS, ArrayList(attachments))
        }
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    companion object {
        const val ARG_ATTACHMENTS = "attachments"
        const val ARG_PATHS = "paths"

        fun newInstance() = AttachFragment()
    }
}