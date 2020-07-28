package com.fdev.vkclient.chatowner.model

import android.content.Context
import com.fdev.vkclient.model.Conversation
import com.fdev.vkclient.model.Group
import com.fdev.vkclient.model.User

/**
 * represents an instance that can have a chat: [User], [Conversation] or [Group]
 */
interface ChatOwner {

    fun getPeerId(): Int
    fun getAvatar(): String?
    fun getTitle(): String
    fun getInfoText(context: Context): String
    fun getPrivacyInfo(context: Context): String?
}