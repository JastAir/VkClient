package com.fdev.vkclient.chats.attachments.stickersemoji.model.network

import com.google.gson.annotations.SerializedName
import com.fdev.vkclient.model.attachments.Sticker

data class Pack(

        val id: Int,

        @SerializedName("title")
        val name: String,

        val active: Int,

        val stickers: List<Sticker>
)