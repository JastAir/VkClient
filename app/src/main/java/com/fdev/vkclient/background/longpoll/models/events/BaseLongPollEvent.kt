package com.fdev.vkclient.background.longpoll.models.events

abstract class BaseLongPollEvent {

    abstract fun getType(): Int

    companion object {
        const val TYPE_INSTALL_FLAGS = 2
        const val TYPE_NEW_MESSAGE = 4
        const val TYPE_EDIT_MESSAGE = 5
        const val TYPE_READ_INCOMING = 6
        const val TYPE_READ_OUTGOING = 7
        const val TYPE_ONLINE = 8
        const val TYPE_OFFLINE = 9
        const val TYPE_DELETE_MESSAGES = 13
        const val TYPE_TYPING = 61
        const val TYPE_TYPING_CHAT = 62
        const val TYPE_RECORDING_AUDIO = 64
        const val TYPE_COUNT = 80

    }
}