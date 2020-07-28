package com.fdev.vkclient.chats.attachments.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fdev.vkclient.chats.attachments.audios.AudioAttachmentsViewModel
import com.fdev.vkclient.chats.attachments.docs.DocAttachmentsViewModel
import com.fdev.vkclient.chats.attachments.links.LinkAttachmentsViewModel
import com.fdev.vkclient.chats.attachments.photos.PhotoAttachmentsViewModel
import com.fdev.vkclient.chats.attachments.videos.VideoAttachmentsViewModel
import com.fdev.vkclient.model.WrappedLiveData
import com.fdev.vkclient.model.WrappedMutableLiveData
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.model.attachments.Attachment
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.network.response.AttachmentsResponse
import com.fdev.vkclient.utils.subscribeSmart
import javax.inject.Inject

abstract class BaseAttachmentsViewModel<T : Any>(protected val api: ApiService) : ViewModel() {

    protected val attachmentsLiveData = WrappedMutableLiveData<ArrayList<T>>()
    private var startFrom: String? = null

    var peerId: Int = 0
        set(value) {
            if (peerId == 0) {
                field = value
            }
        }

    abstract val mediaType: String

    abstract fun convert(attachment: Attachment?): T?

    fun getAttachments() = attachmentsLiveData as WrappedLiveData<ArrayList<T>>

    fun loadAttachments() {
        api.getHistoryAttachments(peerId, mediaType, COUNT, startFrom)
                .subscribeSmart(::onAttachmentsLoaded, { error ->
                    attachmentsLiveData.value = Wrapper(error = error)
                })
    }

    fun reset() {
        startFrom = null
    }

    private fun onAttachmentsLoaded(response: AttachmentsResponse) {
        val existing = if (startFrom.isNullOrEmpty()) {
            arrayListOf()
        } else {
            attachmentsLiveData.value?.data ?: arrayListOf()
        }
        startFrom = response.nextFrom
        val attachments = response.items.mapNotNull { convert(it.attachment) }.distinct()
        attachmentsLiveData.value = Wrapper(existing.apply { addAll(attachments) })
    }

    companion object {
        const val COUNT = 200
    }

    class Factory @Inject constructor(
            private val api: ApiService,
            private val context: Context
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel?> create(modelClass: Class<VM>): VM = when (modelClass) {
            DocAttachmentsViewModel::class.java -> DocAttachmentsViewModel(api) as VM
            LinkAttachmentsViewModel::class.java -> LinkAttachmentsViewModel(api) as VM
            VideoAttachmentsViewModel::class.java -> VideoAttachmentsViewModel(api) as VM
            PhotoAttachmentsViewModel::class.java -> PhotoAttachmentsViewModel(api) as VM
            AudioAttachmentsViewModel::class.java -> AudioAttachmentsViewModel(api, context) as VM

            else -> throw IllegalArgumentException("Unknown class $modelClass")

        }

    }
}