package com.fdev.vkclient.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.fdev.vkclient.model.messages.Message
import com.fdev.vkclient.utils.matchesChatId
import com.fdev.vkclient.utils.matchesGroupId

data class DialogMessage(

        @SerializedName("conversation")
        @Expose
        val conversation: Conversation,

        @SerializedName("last_message")
        @Expose
        val lastMessage: Message
) {
    fun isChat() = lastMessage.peerId.matchesChatId()

    fun isGroup() = lastMessage.peerId.matchesGroupId()
}