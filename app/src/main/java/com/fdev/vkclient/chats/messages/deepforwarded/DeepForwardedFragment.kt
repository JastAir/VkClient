package com.fdev.vkclient.chats.messages.deepforwarded

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.base.BaseFragment
import com.fdev.vkclient.chatowner.ChatOwnerActivity
import com.fdev.vkclient.chats.messages.Interaction
import com.fdev.vkclient.chats.messages.base.BaseMessagesViewModel
import com.fdev.vkclient.chats.messages.base.MessagesAdapter
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.model.attachments.Doc
import com.fdev.vkclient.model.attachments.Photo
import com.fdev.vkclient.model.attachments.Video
import com.fdev.vkclient.model.messages.Message
import com.fdev.vkclient.photoviewer.ImageViewerActivity
import com.fdev.vkclient.utils.hide
import com.fdev.vkclient.utils.showError
import com.fdev.vkclient.web.VideoViewerActivity
import kotlinx.android.synthetic.main.fragment_deep_forwarded.*
import javax.inject.Inject

class DeepForwardedFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: BaseMessagesViewModel.Factory
    private lateinit var viewModel: DeepForwardedViewModel

    private val messageId by lazy { arguments?.getInt(ARG_MESSAGE_ID) }
    private val adapter by lazy {
        MessagesAdapter(contextOrThrow, ::loadMore, ForwardedCallback(), getSettings())
    }

    override fun getLayoutId() = R.layout.fragment_deep_forwarded

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DeepForwardedViewModel::class.java]
        viewModel.getInteraction().observe(this, Observer { onMessageLoaded(it) })
        viewModel.loadMessage(messageId ?: 0)
        rvForwarded.layoutManager = LinearLayoutManager(context)
        rvForwarded.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle(getString(R.string.forwarded_message))
    }

    private fun onMessageLoaded(data: Wrapper<Interaction>) {
        if (data.data != null) {
            if (data.data.type == Interaction.Type.ADD) {
                progressBar.hide()
                adapter.add(data.data.messages.first())
            }
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        adapter.stopLoading(finished = true)
    }

    private fun getSettings() = MessagesAdapter.Settings(
            isImportant = false,
            fullDeepness = true
    )

    companion object {

        const val ARG_MESSAGE_ID = "messageId"

        fun newInstance(arguments: Bundle?): DeepForwardedFragment {
            val fragment = DeepForwardedFragment()
            fragment.arguments = arguments
            return fragment
        }
    }

    private inner class ForwardedCallback : MessagesAdapter.Callback {

        override fun onClicked(message: Message) {

        }

        override fun onUserClicked(userId: Int) {
            ChatOwnerActivity.launch(context, userId)
        }

        override fun onEncryptedFileClicked(doc: Doc) {

        }

        override fun onPhotoClicked(position: Int, photos: ArrayList<Photo>) {
            ImageViewerActivity.viewImages(context, photos, position)
        }

        override fun onVideoClicked(video: Video) {
            context?.also {
                viewModel.loadVideo(it, video, { player ->
                    VideoViewerActivity.launch(it, player)
                }, { error ->
                    showError(it, error)
                })
            }
        }
    }
}