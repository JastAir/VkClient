package com.fdev.vkclient.chats.tools

import androidx.appcompat.widget.Toolbar
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.utils.*
import kotlinx.android.synthetic.main.toolbar_chat.view.*
import rx.Completable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class ChatToolbarController(private val toolbar: Toolbar) {

    private var lastAction = 0
    private var actionSubscription: Subscription? = null

    fun setTitle(title: String) {
        toolbar.tvTitle.text = title
        if (Prefs.lowerTexts) toolbar.tvTitle.lower()
    }

    fun setSubtitle(subtitle: String) {
        toolbar.tvSubtitle.text = subtitle
    }

    fun setAvatar(photo: String?) {
        toolbar.ivAvatar.load(photo)
    }

    fun showActivity() {
        with(toolbar) {
            typingView.show()
            tvSubtitle.setVisibleWithInvis(false)
        }
        startTimer()
    }

    fun hideActions() {
        actionSubscription?.unsubscribe()
        hide()
    }

    private fun startTimer() {
        actionSubscription?.unsubscribe()
        lastAction = time()
        actionSubscription = Completable.timer(ACTION_DELAY_MS, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    if (time() - lastAction >= ACTION_DELAY_S) {
                        hide()
                    }
                }
    }

    private fun hide() {
        with(toolbar) {
            tvSubtitle.show()
            typingView.hide()
        }
    }

    companion object {
        private const val ACTION_DELAY_MS = 5500L
        private const val ACTION_DELAY_S = 5
    }
}