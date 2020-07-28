package com.fdev.vkclient.chats.attachments.photos

import com.fdev.vkclient.chats.attachments.base.BaseAttachViewModel
import com.fdev.vkclient.managers.Session
import com.fdev.vkclient.model.attachments.Photo
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.subscribeSmart

class PhotoAttachViewModel(private val api: ApiService) : BaseAttachViewModel<Photo>() {

    override fun loadAttach(offset: Int) {
        api.getPhotos(Session.uid, "saved", COUNT, offset)
                .subscribeSmart({ response ->
                    onAttachmentsLoaded(offset, ArrayList(response.items))
                }, ::onErrorOccurred)
    }
}