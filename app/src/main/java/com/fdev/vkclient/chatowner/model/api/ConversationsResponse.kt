package com.fdev.vkclient.chatowner.model.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.fdev.vkclient.model.Conversation
import com.fdev.vkclient.model.User

data class ConversationsResponse(

        @SerializedName("items")
        @Expose
        val items: ArrayList<Conversation> = arrayListOf(),

        @SerializedName("profiles")
        @Expose
        val profiles: ArrayList<User> = arrayListOf()
)