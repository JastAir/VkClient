package com.fdev.vkclient.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fdev.vkclient.dialogs.models.Dialog
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.WrappedLiveData
import com.fdev.vkclient.model.WrappedMutableLiveData
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.network.response.BaseResponse
import com.fdev.vkclient.network.response.ListResponse
import com.fdev.vkclient.network.response.SearchConversationsResponse
import com.fdev.vkclient.utils.subscribeSmart
import io.reactivex.Flowable
import io.reactivex.functions.Function3
import javax.inject.Inject

class SearchViewModel(private val api: ApiService) : ViewModel() {

    private val resultLiveData = WrappedMutableLiveData<ArrayList<Dialog>>()

    fun getResult() = resultLiveData as WrappedLiveData<ArrayList<Dialog>>

    fun search(q: String) {
        if (q.isEmpty()) {
            api.searchUsers(q, User.FIELDS, COUNT, 0)
                    .subscribeSmart({ response ->
                        resultLiveData.value = Wrapper(ArrayList(response.items.map { createFromUser(it) }))
                    }, { error ->
                        resultLiveData.value = Wrapper(error = error)
                    })
        } else {
            Flowable.zip(
                    api.searchFriends(q, User.FIELDS, COUNT, 0),
                    api.searchUsers(q, User.FIELDS, COUNT, 0),
                    api.searchConversations(q, COUNT),
                    ResponseCombinerFunction()
            )
                    .subscribeSmart({ response ->
                        resultLiveData.value = Wrapper(ArrayList(response.distinctBy { it.peerId }))
                    }, { error ->
                        resultLiveData.value = Wrapper(error = error)
                    })
        }
    }

    private fun createFromUser(user: User) = Dialog(
            peerId = user.id,
            title = user.fullName,
            photo = user.photo100,
            isOnline = user.isOnline
    )

    companion object {

        const val COUNT = 50
    }

    private inner class ResponseCombinerFunction :
            Function3<BaseResponse<ListResponse<User>>,
                    BaseResponse<ListResponse<User>>,
                    BaseResponse<SearchConversationsResponse>,
                    BaseResponse<ArrayList<Dialog>>> {

        override fun apply(
                friends: BaseResponse<ListResponse<User>>,
                users: BaseResponse<ListResponse<User>>,
                conversations: BaseResponse<SearchConversationsResponse>
        ): BaseResponse<ArrayList<Dialog>> {
            val dialogs = arrayListOf<Dialog>()

            val cResp = conversations.response
            friends.response?.items?.forEach { dialogs.add(createFromUser(it)) }
            cResp?.items?.forEach { conversation ->
                dialogs.add(Dialog(
                        peerId = conversation.peer?.id ?: 0,
                        title = cResp.getTitleFor(conversation) ?: "",
                        photo = cResp.getPhotoFor(conversation) ?: "",
                        isOnline = cResp.isOnline(conversation)
                ))
            }
            users.response?.items?.forEach { dialogs.add(createFromUser(it)) }

            return BaseResponse(dialogs)
        }
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == SearchViewModel::class.java) {
                return SearchViewModel(api) as T
            }
            throw IllegalArgumentException("Unknown ViewModel $modelClass")
        }
    }
}