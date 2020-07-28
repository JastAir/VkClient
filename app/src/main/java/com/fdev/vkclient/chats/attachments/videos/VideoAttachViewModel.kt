package com.fdev.vkclient.chats.attachments.videos

import com.fdev.vkclient.chats.attachments.base.BaseAttachViewModel
import com.fdev.vkclient.model.attachments.Video
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.subscribeSmart

class VideoAttachViewModel(private val api: ApiService) : BaseAttachViewModel<Video>() {

    override fun loadAttach(offset: Int) {
        api.getVideos("", "", COUNT, offset)
                .subscribeSmart({ response ->
                    onAttachmentsLoaded(offset, ArrayList(response.items))
                }, ::onErrorOccurred)
    }
}