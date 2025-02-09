package com.fdev.vkclient.background.longpoll.models.events

data class ReadIncomingEvent(val peerId: Int,
                             val mid: Int) : BaseLongPollEvent() {

    override fun getType() = TYPE_READ_INCOMING
}