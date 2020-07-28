package com.fdev.vkclient.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fdev.vkclient.background.longpoll.models.events.NewMessageEvent
import com.fdev.vkclient.background.longpoll.models.events.OfflineEvent
import com.fdev.vkclient.background.longpoll.models.events.OnlineEvent
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.WrappedLiveData
import com.fdev.vkclient.model.WrappedMutableLiveData
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.EventBus
import com.fdev.vkclient.utils.matchesUserId
import com.fdev.vkclient.utils.subscribeSmart
import javax.inject.Inject

class FriendsViewModel(private val api: ApiService) : ViewModel() {

    init {
        EventBus.subscribeLongPollEventReceived { event ->
            when (event) {
                is OnlineEvent -> changeStatus(true, event.userId, event.timeStamp, event.deviceCode)
                is OfflineEvent -> changeStatus(false, event.userId, event.timeStamp)
                is NewMessageEvent -> {
                    if (!event.isOut() && event.peerId.matchesUserId()) {
                        changeStatus(true, event.peerId, event.timeStamp)
                    }
                }
            }
        }
    }

    private val friendsLiveData = WrappedMutableLiveData<ArrayList<User>>()

    fun getFriends() = friendsLiveData as WrappedLiveData<ArrayList<User>>

    fun loadFriends(offset: Int = 0) {
        api.getFriends(COUNT, offset)
                .subscribeSmart({ friends ->
                    val existing = if (offset == 0) {
                        arrayListOf()
                    } else {
                        friendsLiveData.value?.data ?: arrayListOf()
                    }
                    existing.addAll(friends.items)
                    friendsLiveData.value = Wrapper(existing)
                }, { error ->
                    friendsLiveData.value = Wrapper(error = error)
                })
    }

    private fun changeStatus(isOnline: Boolean, userId: Int, timeStamp: Int, deviceCode: Int = 0) {
        val user = friendsLiveData.value?.data?.find { it.id == userId } ?: return
        user.isOnline = isOnline
        user.lastSeen?.time = timeStamp
        if (deviceCode != 0) {
            user.lastSeen?.platform = deviceCode
        }

        friendsLiveData.value = Wrapper(friendsLiveData.value?.data)
    }

    companion object {
        const val COUNT = 50
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) = FriendsViewModel(api) as T
    }
}