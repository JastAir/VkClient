package com.fdev.vkclient.chats.attachments.base

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fdev.vkclient.chats.attachments.docs.DocAttachViewModel
import com.fdev.vkclient.chats.attachments.gallery.GalleryViewModel
import com.fdev.vkclient.chats.attachments.photos.PhotoAttachViewModel
import com.fdev.vkclient.chats.attachments.videos.VideoAttachViewModel
import com.fdev.vkclient.model.WrappedLiveData
import com.fdev.vkclient.model.WrappedMutableLiveData
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.network.ApiService
import javax.inject.Inject

abstract class BaseAttachViewModel<T : Any> : ViewModel() {

    protected val attachLiveData = WrappedMutableLiveData<ArrayList<T>>()

    abstract fun loadAttach(offset: Int = 0)

    fun getAttach() = attachLiveData as WrappedLiveData<ArrayList<T>>

    protected fun onAttachmentsLoaded(offset: Int, response: ArrayList<T>) {
        val existing = if (offset == 0) {
            arrayListOf()
        } else {
            attachLiveData.value?.data ?: arrayListOf()
        }

        attachLiveData.value = Wrapper(existing.apply { addAll(response) })
    }

    protected fun onErrorOccurred(error: String) {
        attachLiveData.value = Wrapper(error = error)
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
            PhotoAttachViewModel::class.java -> PhotoAttachViewModel(api) as VM
            GalleryViewModel::class.java -> GalleryViewModel(context) as VM
            DocAttachViewModel::class.java -> DocAttachViewModel(api) as VM
            VideoAttachViewModel::class.java -> VideoAttachViewModel(api) as VM

            else -> throw IllegalArgumentException("Unknown class $modelClass")

        }
    }
}