package com.fdev.vkclient.chats.attachments.stickers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.fdev.vkclient.model.attachments.Sticker
import com.fdev.vkclient.utils.FileStorage

class StickersStorage(
        context: Context,
        type: Type
) : FileStorage<ArrayList<Sticker>>(context, type.name + "_v3") {

    private val gson = Gson()

    override fun serialize(data: ArrayList<Sticker>) = gson.toJson(StickerWrapper(data))

    override fun deserialize(s: String) = try {
        gson.fromJson(s, StickerWrapper::class.java).stickers
    } catch (e: Exception) {
        e.printStackTrace()
        arrayListOf<Sticker>()
    }

    enum class Type {
        AVAILABLE,
        RECENT
    }

    data class StickerWrapper(
            @SerializedName("stickers_list")
            val stickers: ArrayList<Sticker>
    )
}