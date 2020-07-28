package com.fdev.vkclient.chatowner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fdev.vkclient.App
import com.fdev.vkclient.chatowner.model.ChatOwner
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.managers.Session
import com.fdev.vkclient.model.*
import com.fdev.vkclient.model.attachments.Photo
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.asChatId
import com.fdev.vkclient.utils.subscribeSmart
import javax.inject.Inject

class ChatOwnerViewModel : ViewModel() {

    @Inject
    lateinit var api: ApiService

    private val chatOwnerLiveData = WrappedMutableLiveData<ChatOwner>()
    private val photosLiveData = MutableLiveData<List<Photo>>()
    private val conversationMembersLiveData = MutableLiveData<List<User>>()
    private val titleLiveData = WrappedMutableLiveData<String>()
    private val blockedLiveData = WrappedMutableLiveData<Boolean>()

    val chatOwner: WrappedLiveData<ChatOwner>
        get() = chatOwnerLiveData

    val photos: LiveData<List<Photo>>
        get() = photosLiveData

    val conversationMembers: LiveData<List<User>>
        get() = conversationMembersLiveData

    val title: WrappedLiveData<String>
        get() = titleLiveData

    val blocked: WrappedLiveData<Boolean>
        get() = blockedLiveData

    init {
        App.appComponent?.inject(this)
    }

    fun <T : ChatOwner> loadChatOwner(peerId: Int, chatOwnerClass: Class<T>) {
        when (chatOwnerClass) {
            User::class.java -> loadUser(peerId)
            Group::class.java -> loadGroup(-peerId)
            Conversation::class.java -> loadConversation(peerId)
        }
    }

    fun <T : ChatOwner> loadPhotos(peerId: Int, chatOwnerClass: Class<T>) {
        api.getPhotos(peerId, PHOTOS_ALBUM, PHOTOS_COUNT)
                .subscribeSmart({
                    photosLiveData.value = it.items
                }, { error ->
                    Lg.wtf("cant load photos for $peerId: $error")
                })
    }

    fun loadChatMembers(peerId: Int) {
        api.getConversationMembers(peerId)
                .subscribeSmart({
                    conversationMembersLiveData.value = it.profiles
                }, { error ->
                    Lg.wtf("cant load members for $peerId: $error")
                })
    }

    fun changeChatTitle(peerId: Int, newTitle: String) {
        api.editChatTitle(peerId.asChatId(), newTitle)
                .subscribeSmart({
                    if (it == 1) {
                        titleLiveData.value = Wrapper(newTitle)
                    }
                }, { error ->
                    titleLiveData.value = Wrapper(error = error)
                })
    }

    fun kickUser(peerId: Int, userId: Int) {
        api.kickUser(peerId.asChatId(), userId)
                .subscribeSmart({
                    if (it == 1) {
                        val members = conversationMembers.value ?: arrayListOf()
                        conversationMembersLiveData.value = members.filter { it.id != userId }
                    }
                }, {})
    }

    fun leaveConversation(peerId: Int) {
        kickUser(peerId, Session.uid)
    }

    fun blockUser(userId: Int) {
        api.blockUser(userId)
                .subscribeSmart({
                    blockedLiveData.value = Wrapper(it == 1)
                }, { error ->
                    blockedLiveData.value = Wrapper(error = error)
                })
    }

    fun unblockUser(userId: Int) {
        api.unblockUser(userId)
                .subscribeSmart({
                    blockedLiveData.value = Wrapper(it != 1)
                }, { error ->
                    blockedLiveData.value = Wrapper(error = error)
                })
    }

    fun getShowNotifications(peerId: Int) = peerId !in Prefs.muteList

    fun setShowNotifications(peerId: Int, show: Boolean) {
        val muteList = Prefs.muteList
        val inMuteList = peerId in muteList
        when {
            show && inMuteList -> {
                muteList.remove(peerId)
            }
            !show && !inMuteList -> {
                muteList.add(peerId)
            }
        }
        Prefs.muteList = muteList
    }

    private fun loadUser(userId: Int) {
        api.getUsers(userId.toString())
                .subscribeSmart({
                    val user = it.getOrNull(0)
                    blockedLiveData.value = Wrapper(user?.blacklistedByMe == 1)
                    chatOwnerLiveData.value = Wrapper(user)
                }, { error ->
                    blockedLiveData.value = Wrapper(false)
                    chatOwnerLiveData.value = Wrapper(error = error)
                })
    }

    private fun loadGroup(id: Int) {
        api.getGroups(id.toString())
                .subscribeSmart({
                    chatOwnerLiveData.value = Wrapper(it.getOrNull(0))
                }, { error ->
                    chatOwnerLiveData.value = Wrapper(error = error)
                })
    }

    private fun loadConversation(peerId: Int) {
        api.getConversationsById(peerId.toString())
                .subscribeSmart({
                    chatOwnerLiveData.value = Wrapper(it.items.getOrNull(0))
                }, { error ->
                    chatOwnerLiveData.value = Wrapper(error = error)
                })
    }

    companion object {
        const val PHOTOS_ALBUM = "profile"
        const val PHOTOS_COUNT = 100
    }

}