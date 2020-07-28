package com.fdev.vkclient.chats.attachments.docs

import com.fdev.vkclient.chats.attachments.base.BaseAttachViewModel
import com.fdev.vkclient.model.attachments.Doc
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.subscribeSmart

class DocAttachViewModel(private val api: ApiService) : BaseAttachViewModel<Doc>() {

    override fun loadAttach(offset: Int) {
        api.getDocs(COUNT, offset)
                .subscribeSmart({ response ->
                    onAttachmentsLoaded(offset, ArrayList(response.items))
                }, ::onErrorOccurred)
    }
}