package com.fdev.vkclient.chats.messages.starred

import com.fdev.vkclient.chats.messages.Interaction
import com.fdev.vkclient.chats.messages.base.BaseMessagesViewModel
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.model.messages.Message
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.network.response.BaseResponse
import com.fdev.vkclient.network.response.MessagesResponse
import com.fdev.vkclient.utils.subscribeSmart

class StarredMessagesViewModel(api: ApiService) : BaseMessagesViewModel(api) {

    override fun loadMessages(offset: Int) {
        api.getStarredMessages(COUNT, offset)
                .map { convert(it) }
                .subscribeSmart({
                    onMessagesLoaded(it, offset)
                }, ::onErrorOccurred)
    }

    fun unmarkMessage(message: Message) {
        api.markMessagesAsImportant("${message.id}", 0)
                .subscribeSmart({ response ->
                    if (response.getOrNull(0) == message.id) {
//                        messagesLiveData.value?.data?.remove(message)
//                        messagesLiveData.value = Wrapper(messagesLiveData.value?.data)
                        val pos = messages.indexOf(message)
                        messages.remove(message)
                        interactionsLiveData.value = Wrapper(Interaction(Interaction.Type.REMOVE, pos))
                    }
                }, ::onErrorOccurred)
    }

    private fun convert(resp: BaseResponse<MessagesResponse>): BaseResponse<ArrayList<Message>> {
        val messages = arrayListOf<Message>()
        val response = resp.response
        response?.messages?.items?.forEach {
            val message = putTitles(it, response)
            message.read = true
            messages.add(message)
        }

        return BaseResponse(messages, resp.error)
    }

    private fun putTitles(message: Message, response: MessagesResponse): Message {
        message.name = response.getNameForMessage(message)
        message.photo = response.getPhotoForMessage(message)
        val fwd = arrayListOf<Message>()
        message.fwdMessages?.forEach {
            fwd.add(putTitles(it, response))
        }
        message.fwdMessages?.clear()
        message.fwdMessages?.addAll(fwd)
        return message
    }

    companion object {
        const val COUNT = 200
    }
}