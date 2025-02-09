package com.fdev.vkclient.utils

import com.fdev.vkclient.background.longpoll.models.events.BaseLongPollEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

object EventBus {

    /**
     * longpoll received new event
     */
    private val longPollEventReceived = PublishSubject.create<BaseLongPollEvent>()

    fun subscribeLongPollEventReceived(action: (BaseLongPollEvent) -> Unit) = longPollEventReceived
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(action)

    fun publishLongPollEventReceived(longPollEvent: BaseLongPollEvent) = longPollEventReceived.onNext(longPollEvent)
}