package com.fdev.vkclient.features

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fdev.vkclient.App
import com.fdev.vkclient.accounts.models.Account
import com.fdev.vkclient.db.AppDb
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.managers.Session
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.applySingleSchedulers
import com.fdev.vkclient.utils.subscribeSmart
import javax.inject.Inject

class FeaturesViewModel(
        private val appDb: AppDb,
        private val api: ApiService
) : ViewModel() {

    private val accountLiveData = MutableLiveData<Account>()

    fun getAccount() = accountLiveData as LiveData<Account>

    @SuppressLint("CheckResult")
    fun loadAccount() {
        appDb.accountsDao().getRunningAccount()
                .compose(applySingleSchedulers())
                .subscribe({ account ->
                    accountLiveData.value = account
                }, {
                    it.printStackTrace()
                    Lg.wtf("[features] error loading account: ${it.message}")
                })
    }

    fun shareXvii(onSuccess: () -> Unit, onError: (String) -> Unit) {
        api.repost(App.SHARE_POST)
                .subscribeSmart({
                    onSuccess()
                }, onError)
    }

    fun checkMembership(callback: (Boolean) -> Unit) {
        api.isGroupMember(App.GROUP, Session.uid)
                .subscribeSmart({
                    callback.invoke(it == 1)
                }, {
                    error ->
                    Lg.wtf("check membership error: $error")
                })
    }

    fun joinGroup() {
        api.joinGroup(App.GROUP)
                .subscribeSmart({}, {})
    }

    class Factory @Inject constructor(
            private val appDb: AppDb,
            private val api: ApiService
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass == FeaturesViewModel::class.java) {
                return FeaturesViewModel(appDb, api) as T
            }
            throw IllegalArgumentException("Unknown ViewModel $modelClass")
        }
    }

}