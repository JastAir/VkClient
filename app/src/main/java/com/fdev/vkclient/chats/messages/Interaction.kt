package com.fdev.vkclient.chats.messages

import com.fdev.vkclient.model.messages.Message

/**
 * for interaction with messages
 */
data class Interaction(
        val type: Type,

        /**
         * from which to apply changes
         */
        val position: Int = 0,

        /**
         * changed messages in natural ui order (eldest first)
         */
        val messages: List<Message> = arrayListOf()
) {

    enum class Type {
        ADD,
        UPDATE,
        REMOVE,
        CLEAR
    }
}