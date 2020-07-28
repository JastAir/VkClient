package com.fdev.vkclient.chats.attachments.photos

import com.fdev.vkclient.chats.attachments.base.BaseAttachmentsViewModel
import com.fdev.vkclient.model.attachments.Attachment
import com.fdev.vkclient.model.attachments.Photo
import com.fdev.vkclient.network.ApiService

class PhotoAttachmentsViewModel(api: ApiService) : BaseAttachmentsViewModel<Photo>(api) {

    override val mediaType = "photo"

    override fun convert(attachment: Attachment?) = attachment?.photo
}