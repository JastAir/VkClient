package com.fdev.vkclient.chats.attachments.videos

import com.fdev.vkclient.chats.attachments.base.BaseAttachmentsViewModel
import com.fdev.vkclient.model.attachments.Attachment
import com.fdev.vkclient.model.attachments.Video
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.subscribeSmart

class VideoAttachmentsViewModel(api: ApiService) : BaseAttachmentsViewModel<Video>(api) {

    override val mediaType = "video"

    override fun convert(attachment: Attachment?) = attachment?.video

    fun loadVideoPlayer(
            video: Video,
            onPlayerLoader: (String) -> Unit,
            onError: (String?) -> Unit
    ) {
        api.getVideos(video.videoId, video.accessKey)
                .subscribeSmart({ response ->
                    val player = response.items.getOrNull(0)?.player
                    if (player != null) {
                        onPlayerLoader(player)
                    } else {
                        onError(null)
                    }
                }, onError)
    }
}