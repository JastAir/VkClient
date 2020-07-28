package com.fdev.vkclient.chats.attachments.links

import com.fdev.vkclient.chats.attachments.base.BaseAttachmentsViewModel
import com.fdev.vkclient.model.attachments.Attachment
import com.fdev.vkclient.model.attachments.Link
import com.fdev.vkclient.network.ApiService

class LinkAttachmentsViewModel(api: ApiService) : BaseAttachmentsViewModel<Link>(api) {

    override val mediaType = "link"

    override fun convert(attachment: Attachment?) = attachment?.link
}