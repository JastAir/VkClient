package com.fdev.vkclient.model.messages

import android.content.Context
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.fdev.vkclient.R
import com.fdev.vkclient.background.longpoll.models.events.BaseMessageEvent
import com.fdev.vkclient.model.attachments.*
import com.fdev.vkclient.utils.matchesChatId
import com.fdev.vkclient.utils.time
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Message(

        @SerializedName("id")
        @Expose
        val id: Int = 0,

        @SerializedName("peer_id")
        @Expose
        val peerId: Int = 0,

        @SerializedName("date")
        @Expose
        val date: Int = 0,

        @SerializedName("from_id")
        @Expose
        val fromId: Int = 0,

        @SerializedName("text")
        @Expose
        var text: String = "",

        @SerializedName("out")
        @Expose
        val out: Int = 0,

        @SerializedName("action")
        @Expose
        val action: MessageAction? = null,
//
//        @SerializedName("action_text")
//        @Expose
//        val actionText: String? = null,
//
//        @SerializedName("action_mid")
//        @Expose
//        val actionMid: String? = null,

        @SerializedName("fwd_messages")
        @Expose
        val fwdMessages: ArrayList<Message>? = arrayListOf(),

        @SerializedName("attachments")
        @Expose
        val attachments: ArrayList<Attachment>? = arrayListOf(),

        @SerializedName("reply_message")
        @Expose
        var replyMessage: Message? = null,

        @SerializedName("update_time")
        @Expose
        var updateTime: Int = 0,

        @SerializedName("important")
        val important: Boolean = false,

        // ------------------- manually added values
        @SerializedName("read")
        @Expose
        var read: Boolean = false,

        @SerializedName("name")
        @Expose
        var name: String? = null,

        @SerializedName("photo")
        @Expose
        var photo: String? = null
) : Parcelable {

    constructor(event: BaseMessageEvent, prepareText: (String) -> String = { it }) : this(
            id = event.id,
            peerId = event.peerId,
            date = event.timeStamp,
            fromId = event.info.from,
            text = prepareText(event.text),
            out = if (event.isOut()) 1 else 0
    )

    fun isEdited() = updateTime != 0

    fun isOut() = out == 1

    fun isSystem() = action != null

    fun isSticker() = attachments != null && attachments.isSticker() && replyMessage == null

    fun isGraffiti() = attachments != null && attachments.isGraffiti()

    fun isGift() = attachments != null && attachments.isGift() && replyMessage == null

    fun isReplyingSticker() = attachments != null && attachments.isSticker() && replyMessage != null

    fun hasPhotos() = attachments != null && attachments.photosCount() > 0

    fun isChat() = peerId.matchesChatId()

    fun isFresh() = time() - date < DAY

    fun isEditable() = isOut() && isFresh() && !isSticker()

    fun isDeletableForAll() = isOut() && isFresh()

    fun getResolvedMessage(context: Context?): String = when {
        context == null || text.isNotBlank() -> text
        !attachments.isNullOrEmpty() -> {
            val count = attachments.size
            when {
                attachments.isSticker() -> context.getString(R.string.sticker)
                attachments.isGraffiti() -> context.getString(R.string.graffiti)
                attachments.isGift() -> context.getString(R.string.gift_for_you)
                attachments.isAudioMessage() -> context.getString(R.string.voice_message)
                attachments.isPoll() -> context.getString(R.string.poll)
                attachments.isLink() -> context.getString(R.string.link)
                attachments.isWallPost() -> context.getString(R.string.wall_post)

                attachments.photosCount() != 0 ->
                    context.resources.getQuantityString(R.plurals.attachments_photos, count, count)

                attachments.videosCount() != 0 ->
                    context.resources.getQuantityString(R.plurals.attachments_videos, count, count)

                attachments.audiosCount() != 0 ->
                    context.resources.getQuantityString(R.plurals.attachments_audios, count, count)

                attachments.docsCount() != 0 ->
                    context.resources.getQuantityString(R.plurals.attachments_docs, count, count)

                else -> context.resources.getQuantityString(R.plurals.attachments, count, count)
            }
        }
        !fwdMessages.isNullOrEmpty() -> context.getString(R.string.forwarded_messages)
        else -> text
    }

    companion object {
        const val DAY = 3600 * 24
    }
}