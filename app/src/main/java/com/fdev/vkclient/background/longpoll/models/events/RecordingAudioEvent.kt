package com.fdev.vkclient.background.longpoll.models.events

data class RecordingAudioEvent(val peerId: Int) : BaseLongPollEvent() {

    override fun getType() = TYPE_RECORDING_AUDIO
}