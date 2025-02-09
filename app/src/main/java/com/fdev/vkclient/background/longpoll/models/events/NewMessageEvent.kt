package com.fdev.vkclient.background.longpoll.models.events

class NewMessageEvent(
        id: Int,
        flags: Int,
        peerId: Int,
        timeStamp: Int,
        text: String,
        info: MessageInfo
) : BaseMessageEvent(id, flags, peerId, timeStamp, text, info) {

    override fun getType() = TYPE_NEW_MESSAGE
}