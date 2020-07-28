package com.fdev.vkclient.features.general

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fdev.vkclient.App
import com.fdev.vkclient.db.AppDb
import com.fdev.vkclient.utils.applySingleSchedulers
import com.fdev.vkclient.utils.getCacheSize
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GeneralViewModel : ViewModel() {

    @Inject
    lateinit var applicationContext: Context

    @Inject
    lateinit var appDb: AppDb

    private var disposable: Disposable? = null

    private val cacheSizeLiveData = MutableLiveData<Long>()
    private val cacheClearedLiveData = MutableLiveData<Boolean>()

    val cacheSize: LiveData<Long>
        get() = cacheSizeLiveData

    val cacheCleared: LiveData<Boolean>
        get() = cacheClearedLiveData

    init {
        App.appComponent?.inject(this)
    }

    fun calculateCacheSize() {
        disposable?.dispose()
        disposable = Single.fromCallable {
            getCacheSize(applicationContext)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn { 0L }
                .subscribe { size ->
                    cacheSizeLiveData.value = size
                }
    }

    fun clearCache() {
        disposable?.dispose()
        disposable = Single.fromCallable {
            com.fdev.vkclient.utils.clearCache(applicationContext)
            true
        }
                .flatMap { result ->
                    appDb.stickersDao()
                            .clearStickers()
                            .toSingleDefault(result)
                }
                .compose(applySingleSchedulers())
                .onErrorReturn { false }
                .subscribe { success ->
                    cacheClearedLiveData.value = success
                    if (success) {
                        calculateCacheSize()
                    }
                }
    }

}