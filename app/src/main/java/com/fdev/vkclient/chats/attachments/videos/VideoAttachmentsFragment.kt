package com.fdev.vkclient.chats.attachments.videos

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.chats.attachments.base.BaseAttachmentsFragment
import com.fdev.vkclient.model.attachments.Video
import com.fdev.vkclient.utils.showError
import com.fdev.vkclient.web.VideoViewerActivity

class VideoAttachmentsFragment : BaseAttachmentsFragment<Video>() {

    override val adapter by lazy {
        VideoAttachmentsAdapter(contextOrThrow, ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun getViewModelClass() = VideoAttachmentsViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(video: Video) {
        (viewModel as? VideoAttachmentsViewModel)?.loadVideoPlayer(video, { player ->
            VideoViewerActivity.launch(context, player)
        }) { error ->
            showError(context, error ?: getString(R.string.error))
        }
    }

    companion object {
        fun newInstance(peerId: Int): VideoAttachmentsFragment {
            val fragment = VideoAttachmentsFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}