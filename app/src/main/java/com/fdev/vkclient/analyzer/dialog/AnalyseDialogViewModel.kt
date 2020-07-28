package com.fdev.vkclient.analyzer.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.WrappedLiveData
import com.fdev.vkclient.model.WrappedMutableLiveData
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.subscribeSmart
import javax.inject.Inject

class AnalyseDialogViewModel(private val api: ApiService) : ViewModel() {

    private val userLiveData = WrappedMutableLiveData<User>()
    private val progressLiveData = WrappedMutableLiveData<Pair<Int, Int>>()

    var peerId: Int = 0
        set(value) {
            if (field == 0) {
                field = value
            }
        }

    fun getUser() = userLiveData as WrappedLiveData<User>

    fun getProgress() = progressLiveData as WrappedLiveData<Pair<Int, Int>>

    fun analyse() {
        loadUser()
        loadDialogs()
    }

    private fun loadDialogs() {
        api.getMessagesLite(peerId, COUNT, progressLiveData.value?.data?.first ?: 0)
                .repeat(Long.MAX_VALUE)
                .takeUntil { response ->
                    response.response?.apply {
                        if ((progressLiveData.value?.data?.second ?: 0) == 0) {
                            updateTotalCount(count)
                        }
                        updateLoadedCount(items.size)
                    }
                    isFullyLoaded()
                }
                .subscribeSmart({}, {
                    Lg.wtf(it)
                })
    }

    private fun loadUser() {
        api.getUsers("$peerId")
                .subscribeSmart({ users ->
                    userLiveData.value = Wrapper(users.getOrNull(0))
                }, { error ->
                    userLiveData.value = Wrapper(error = error)
                })
    }

    private fun isFullyLoaded(): Boolean {
        val loadedCount = progressLiveData.value?.data?.first ?: return false
        val totalCount = progressLiveData.value?.data?.second ?: return false
        return loadedCount >= totalCount
    }

    private fun updateTotalCount(totalCount: Int) {
        progressLiveData.value = Wrapper(Pair(0, totalCount))
    }

    private fun updateLoadedCount(deltaLoadedCount: Int) {
        val loadedCount = progressLiveData.value?.data?.first ?: return
        val totalCount = progressLiveData.value?.data?.second ?: return

        progressLiveData.value = Wrapper(Pair(loadedCount + deltaLoadedCount, totalCount))
    }

    companion object {

        const val COUNT = 200
    }

    class Factory @Inject constructor(private val api: ApiService) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == AnalyseDialogViewModel::class.java) {
                return AnalyseDialogViewModel(api) as T
            }
            throw IllegalArgumentException("Unknown ViewModel $modelClass")
        }
    }
}