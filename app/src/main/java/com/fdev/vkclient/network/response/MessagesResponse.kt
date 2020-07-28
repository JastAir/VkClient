package com.fdev.vkclient.network.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.fdev.vkclient.model.Conversation
import com.fdev.vkclient.model.Group
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.messages.Message
import com.fdev.vkclient.utils.matchesChatId
import com.fdev.vkclient.utils.matchesGroupId
import com.fdev.vkclient.utils.matchesUserId

data class MessagesResponse(

        @SerializedName("messages")
        @Expose
        val messages: ListResponse<Message>,

        @SerializedName("profiles")
        @Expose
        val profiles: ArrayList<User>,

        @SerializedName("groups")
        @Expose
        val groups: ArrayList<Group>,

        @SerializedName("conversations")
        @Expose
        val conversations: ArrayList<Conversation>
) {
    fun getProfileById(id: Int) = profiles.find { it.id == id }

    fun getGroupById(id: Int) = groups.find { it.id == id }

    fun getConversationById(id: Int) = conversations.find { it.peer?.id == id }

    fun getNameForMessage(message: Message) = when {
        message.peerId.matchesUserId() -> getProfileById(message.fromId)?.fullName
        message.peerId.matchesGroupId() -> getGroupById(-message.peerId)?.name
        message.peerId.matchesChatId() -> getConversationById(message.peerId)?.chatSettings?.title
        else -> null
    }

    fun getPhotoForMessage(message: Message) = when {
        message.peerId.matchesUserId() -> getProfileById(message.fromId)?.photo100
        message.peerId.matchesGroupId() -> getGroupById(-message.peerId)?.photo100
        message.peerId.matchesChatId() -> getConversationById(message.peerId)?.chatSettings?.photo?.photo100
        else -> null
    }
}