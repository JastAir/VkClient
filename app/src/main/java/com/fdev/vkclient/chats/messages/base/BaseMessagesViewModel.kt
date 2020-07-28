package com.fdev.vkclient.chats.messages.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fdev.vkclient.R
import com.fdev.vkclient.chats.messages.Interaction
import com.fdev.vkclient.chats.messages.chat.secret.SecretChatViewModel
import com.fdev.vkclient.chats.messages.chat.usual.ChatMessagesViewModel
import com.fdev.vkclient.chats.messages.deepforwarded.DeepForwardedViewModel
import com.fdev.vkclient.chats.messages.starred.StarredMessagesViewModel
import com.fdev.vkclient.chats.tools.ChatStorage
import com.fdev.vkclient.model.WrappedLiveData
import com.fdev.vkclient.model.WrappedMutableLiveData
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.model.attachments.Video
import com.fdev.vkclient.model.messages.Message
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.subscribeSmart
import javax.inject.Inject

abstract class BaseMessagesViewModel(protected val api: ApiService) : ViewModel() {

    /**
     * stored in natural ui order: eldest first
     */
    protected val messages = arrayListOf<Message>()

    protected val interactionsLiveData = WrappedMutableLiveData<Interaction>()

    fun getInteraction() = interactionsLiveData as WrappedLiveData<Interaction>

    abstract fun loadMessages(offset: Int = 0)

    fun getStoredMessages() = ArrayList(messages)

    fun loadVideo(
            context: Context,
            video: Video,
            onLoaded: (String) -> Unit,
            onError: (String) -> Unit
    ) {
        api.getVideos(
                video.videoId,
                video.accessKey ?: "",
                1, 0
        )
                .subscribeSmart({ response ->
                    if (response.items.size > 0 && response.items[0].player != null) {
                        onLoaded(response.items[0].player ?: "")
                    } else {
                        onError(context.getString(R.string.not_playable_video))
                    }
                }, onError)
    }

    protected fun onMessagesLoaded(items: ArrayList<Message>, offset: Int = 0) {
        val newMessages = items.reversed()
        if (offset == 0) {
            messages.clear()
            interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.CLEAR))
        }
        messages.addAll(0, newMessages)
        interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.ADD, 0, newMessages))

    }

    protected fun onErrorOccurred(error: String) {
        interactionsLiveData.value = Wrapper(error = error)
    }

    class Factory @Inject constructor(
            private val api: ApiService,
            private val context: Context,
            private val chatStorage: ChatStorage
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = when (modelClass) {
            StarredMessagesViewModel::class.java -> StarredMessagesViewModel(api) as T
            DeepForwardedViewModel::class.java -> DeepForwardedViewModel(api) as T
            ChatMessagesViewModel::class.java -> ChatMessagesViewModel(api, chatStorage) as T
            SecretChatViewModel::class.java -> SecretChatViewModel(api, context) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class $modelClass")
        }
    }
}