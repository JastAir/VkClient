package com.fdev.vkclient.background.longpoll.models.events

data class ReadOutgoingEvent(val peerId: Int,
                             val mid: Int) : BaseLongPollEvent() {

    override fun getType() = TYPE_READ_OUTGOING
}