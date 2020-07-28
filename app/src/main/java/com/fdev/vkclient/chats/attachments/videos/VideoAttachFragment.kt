package com.fdev.vkclient.chats.attachments.videos

import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.App
import com.fdev.vkclient.chats.attachments.base.BaseAttachFragment
import com.fdev.vkclient.model.attachments.Attachment
import com.fdev.vkclient.model.attachments.Video
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class VideoAttachFragment : BaseAttachFragment<Video>() {

    override val adapter by lazy {
        VideoAttachmentsAdapter(contextOrThrow, ::loadMore, ::onClick)
    }

    override fun getLayoutManager() = LinearLayoutManager(context)

    override fun getViewModelClass() = VideoAttachViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    private fun onClick(video: Video) {
        selectedSubject.onNext(arrayListOf(Attachment(video)))
    }

    companion object {
        private val disposables = CompositeDisposable()
        private val selectedSubject = PublishSubject.create<List<Attachment>>()

        fun newInstance(onSelected: (List<Attachment>) -> Unit): VideoAttachFragment {
            selectedSubject.subscribe(onSelected).let { disposables.add(it) }
            return VideoAttachFragment()
        }

        fun clear() {
            disposables.clear()
        }
    }

}