package com.fdev.vkclient.network.response

import com.google.gson.annotations.SerializedName
import com.fdev.vkclient.model.attachments.Attachment


class AttachmentsResponse {

    val items: MutableList<AttachmentsContainer> = mutableListOf()
    @SerializedName("next_from")
    val nextFrom: String? = null

    inner class AttachmentsContainer {
        @SerializedName("message_id")
        var messageId: Int = 0
        var attachment: Attachment? = null
    }

}
