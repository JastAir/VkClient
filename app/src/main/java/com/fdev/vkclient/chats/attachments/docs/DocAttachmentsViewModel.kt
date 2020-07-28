package com.fdev.vkclient.chats.attachments.docs

import com.fdev.vkclient.chats.attachments.base.BaseAttachmentsViewModel
import com.fdev.vkclient.model.attachments.Attachment
import com.fdev.vkclient.model.attachments.Doc
import com.fdev.vkclient.network.ApiService

class DocAttachmentsViewModel(api: ApiService) : BaseAttachmentsViewModel<Doc>(api) {

    override val mediaType = "doc"

    override fun convert(attachment: Attachment?) = attachment?.doc
}