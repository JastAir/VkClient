package com.fdev.vkclient.chats.tools

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.ClipDescription
import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.os.Vibrator
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import com.fdev.vkclient.R
import com.fdev.vkclient.chats.attachments.stickersemoji.StickersEmojiRepository
import com.fdev.vkclient.chats.attachments.stickersemoji.StickersEmojiWindow
import com.fdev.vkclient.chats.attachments.stickersemoji.model.Emoji
import com.fdev.vkclient.chats.messages.chat.base.BaseChatMessagesViewModel
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.attachments.Sticker
import com.fdev.vkclient.utils.*
import kotlinx.android.synthetic.main.chat_input_panel.view.*
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * Created by msnthrp on 17/01/18.
 */
class ChatInputController(
        private val context: Context,
        private val rootView: View,
        private val callback: ChatInputCallback
) {

    private val loadingQueue = arrayListOf<Any>()
    private val stickerKeyboard = StickersEmojiWindow(rootView, context, ::onKeyboardClosed, callback::onStickerClicked, ::addEmoji)
    private val voiceRecorder = VoiceRecorder(context, InputRecorderCallback())
    private val repo by lazy { StickersEmojiRepository() }
    private val stickers = arrayListOf<com.fdev.vkclient.chats.attachments.stickersemoji.model.Sticker>()

    private var attachedCount = 0
    private var lastTypingInvocation = 0
    private var keyboardState = KeyboardState.TEXT

    init {
        with(rootView) {
            ivSend.setOnClickListener { callback.onSendClick() }
            ivKeyboard.setOnClickListener { switchKeyboardState() }
            ivKeyboard.setVisible(Prefs.showStickers)
            ivAttach.setOnClickListener { callback.onAttachClick() }
            pbAttach.hide()
            etInput.addTextChangedListener(ChatTextWatcher())
            etInput.onRichContentAdded = ::onRichContentAdded
            when {
                Prefs.sendByEnter -> {
                    etInput.imeOptions = EditorInfo.IME_ACTION_SEND
                    etInput.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES
                    etInput.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_SEND && Prefs.sendByEnter) {
                            callback.onSendClick()
                            true
                        } else {
                            false
                        }
                    }
                }
                !Prefs.lowerTexts -> {
                    etInput.inputType = etInput.inputType or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                }
            }
            val micListener = MicTouchListener()
            ivMic.setOnTouchListener(micListener)
            ivSendVoice.setOnClickListener { micListener.sendWhenLocked() }
            ivCancelVoice.setOnClickListener { micListener.cancelWhenLocked() }

            ivSend.stylizeAnyway(ColorManager.MAIN_TAG)
            ivMic.stylizeAnyway(ColorManager.MAIN_TAG)
            ivSendVoice.stylizeAnyway(ColorManager.MAIN_TAG)
        }
        stickerKeyboard.setSizeForSoftKeyboard()
        setAttachedCount(0)

        repo.loadRawStickersFromDb {
            stickers.clear()
            stickers.addAll(it)
        }
    }

    fun addItemAsBeingLoaded(item: Any) {
        loadingQueue.add(item)
        invalidateProgress()
    }

    fun removeItemAsLoaded(item: Any) {
        loadingQueue.remove(item)
        invalidateProgress()
    }

    fun setAttachedCount(count: Int) {
        attachedCount = count
        if (count == 0) {
            rootView.rlAttachCount.hide()
            if (rootView.etInput.asText().isBlank() && Prefs.showVoice) {
                switchToMic()
            } else {
                switchToSend()
            }
        } else {
            rootView.rlAttachCount.show()
            val text = if (count == 10) "+" else count.toString()
            rootView.tvAttachCount.text = text
            switchToSend()
        }
    }

    fun mentionUser(user: User) {
        rootView.etInput.apply {
            val input = text.toString()
            val mentionEnd = selectionStart
            var mentionStart = mentionEnd
            do {
                mentionStart--
            } while (mentionStart != 0 && input[mentionStart] != '@')

            var replacement = "@${user.getPageName()}"
            if (user != BaseChatMessagesViewModel.USER_ONLINE
                    && user != BaseChatMessagesViewModel.USER_ALL) {

                replacement += " (${user.firstName})"
            }
            val newInput = StringBuilder()
                    .append(input.substring(0, mentionStart))
                    .append(replacement)
                    .append(input.substring(mentionEnd))
                    .toString()

            setText(newInput)
            setSelection(mentionStart + replacement.length)
            callback.onMention(null) // hide mentioning
        }
    }

    private fun addEmoji(emoji: Emoji) {
        val start = rootView.etInput.selectionStart
        val end = rootView.etInput.selectionEnd
        if (start < 0) {
            rootView.etInput.append(emoji.code)
        } else {
            rootView.etInput.text?.replace(min(start, end),
                    max(start, end), emoji.code, 0,
                    emoji.code.length)
        }
    }

    private fun switchKeyboardState() {
        when (keyboardState) {
            KeyboardState.TEXT -> {
                keyboardState = KeyboardState.STICKERS
                stickerKeyboard.showWithRequest(rootView.etInput)
            }
            KeyboardState.STICKERS -> {
                keyboardState = KeyboardState.TEXT
                stickerKeyboard.dismiss()
            }
        }
        updateKeyboardIcon()
    }

    private fun updateKeyboardIcon() {
        val iconRes = when (keyboardState) {
            KeyboardState.TEXT -> R.drawable.ic_emoji
            KeyboardState.STICKERS -> R.drawable.ic_keyboard
        }
        val d = ContextCompat.getDrawable(context, iconRes)
        rootView.ivKeyboard.setImageDrawable(d)
    }

    private fun invalidateProgress() {
        rootView.pbAttach.setVisible(loadingQueue.isNotEmpty())
    }

    private fun switchToSend() {
        rootView.ivSend.show()
        rootView.ivMic.hide()
    }

    private fun switchToMic() {
        rootView.ivSend.hide()
        rootView.ivMic.show()
    }

    private fun onKeyboardClosed() {
        if (stickerKeyboard.isShowing) {
            stickerKeyboard.dismiss()
        }
        keyboardState = KeyboardState.TEXT
        updateKeyboardIcon()
    }

    private fun onRichContentAdded(uri: Uri, description: ClipDescription) {
        if (description.mimeTypeCount > 0) {
            val fileExtension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(description.getMimeType(0)) ?: return

            val richContentFile = File(context.cacheDir, "richContent.$fileExtension")
            if (!writeToFileFromContentUri(context, richContentFile, uri)) {
                Lg.wtf("error adding rich content")
            } else {
                callback.onRichContentAdded(richContentFile.absolutePath)
            }
        }
    }

    private fun getMatchedStickers(typed: CharSequence): List<Sticker> {
        if (typed.isBlank() || typed.length < 2 && !EmojiHelper.hasEmojis(typed.toString())) {
            return arrayListOf()
        }

        val typedLower = typed.toString().toLowerCase()
        return stickers
                .filter { sticker ->
                    sticker.keyWordsList
                            .map { word -> if (word.startsWith(typedLower)) 1 else 0 }
                            .sum() != 0
                }
                .map { Sticker(it.id) }
    }

    private fun onVoiceRecordingLocked() {
        vibrate()
        rootView.tvMicHint.setVisible(false)
        rootView.ivLocked.setVisible(true)
        rootView.rlLockedButtons.setVisible(true)
    }

    private fun vibrate() {
        val vibrate = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrate.vibrate(20L)
    }

    companion object {
        const val TYPING_INVOCATION_DELAY = 5 // seconds
    }

    /**
     * invokes end of recording
     * supports cancelling by swipe
     */
    private inner class MicTouchListener : View.OnTouchListener {

        /**
         * threshold to cancel
         */
        private val cancelThreshold = 200

        /**
         * threshold to lock
         */
        private val lockThreshold = 300
        private val delayTimer = MicClickTimer {
            if (callback.hasMicPermissions()) {
                voiceRecorder.startRecording()
            }
        }

        /**
         * to watch if cancelled
         */
        private var xPress = 0f

        /**
         * to watch if locked
         */
        private var yPress = 0f
        private var alreadyStopped = false

        /**
         * lock flag
         */
        private var locked = false

        override fun onTouch(v: View?, event: MotionEvent?) = when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                xPress = event.x
                yPress = event.y

                delayTimer.start()
                alreadyStopped = false
                locked = false
                true
            }

            MotionEvent.ACTION_MOVE -> {
                when {
                    !locked && shouldLock(event) -> {
                        locked = true
                        onVoiceRecordingLocked()
                        false
                    }
                    shouldCancel(event) -> {
                        stop(cancel = true)
                        true
                    }
                    else -> false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!alreadyStopped && !locked) {
                    stop(cancel = false)
                }
                true
            }
            else -> true
        }

        fun cancelWhenLocked() {
            if (locked && !alreadyStopped) {
                stop(cancel = true)
            }
        }

        fun sendWhenLocked() {
            if (locked && !alreadyStopped) {
                stop(cancel = false)
            }
        }

        private fun stop(cancel: Boolean) {
            alreadyStopped = true
            delayTimer.cancel()
            voiceRecorder.stopRecording(cancel)
        }

        private fun shouldCancel(event: MotionEvent) = abs(xPress - event.x) > cancelThreshold

        private fun shouldLock(event: MotionEvent) = abs(yPress - event.y) > lockThreshold
    }

    /**
     * adds delay before invoking
     */
    private inner class MicClickTimer(private val callback: () -> Unit)
        : CountDownTimer(150L, 150L) {
        override fun onFinish() {
            callback.invoke()
        }

        override fun onTick(millisUntilFinished: Long) {}
    }

    private inner class ChatTextWatcher : TextWatcher {

        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val text = s ?: ""

            if (text.isBlank() && attachedCount == 0 && Prefs.showVoice) {
                switchToMic()
            } else {
                switchToSend()
            }
            if (Prefs.stickerSuggestions) {
                callback.onStickersSuggested(getMatchedStickers(text))
            }

            if ('@' in text) {
                val words = text.split(" ")
                if (words.isNotEmpty()) {
                    val lastWord = words.last()
                    if (lastWord.startsWith('@')) {
                        callback.onMention(lastWord.substring(1))
                    }
                }
            } else {
                callback.onMention(null)
            }

            val delayExceed = time() - lastTypingInvocation > TYPING_INVOCATION_DELAY
            if (delayExceed && text.isNotBlank()) {
                callback.onTypingInvoke()
                lastTypingInvocation = time()
            }
        }
    }

    /**
     * for interacting with [ChatFragment]
     */
    interface ChatInputCallback {
        fun onStickerClicked(sticker: Sticker)
        fun onSendClick()
        fun hasMicPermissions(): Boolean
        fun onAttachClick()
        fun onTypingInvoke()
        fun onVoiceRecordingInvoke()
        fun onRichContentAdded(filePath: String)
        fun onStickersSuggested(stickers: List<Sticker>)
        fun onVoiceRecorded(fileName: String)
        fun onMention(query: String?)
    }

    private inner class InputRecorderCallback : VoiceRecorder.RecorderCallback {

        private var lastVoiceInvoke = -5

        override fun onVoiceVisibilityChanged(visible: Boolean) {
            rootView.rlVoice.setVisible(visible)
            if (!visible) {
                rootView.tvMicHint.setVisible(true)
                rootView.ivLocked.setVisible(false)
                rootView.rlLockedButtons.setVisible(false)
            }
        }

        override fun onVoiceTimeUpdated(time: Int) {
            rootView.tvRecordTime.text = secToTime(time)
            if (time - lastVoiceInvoke >= 5) {
                callback.onVoiceRecordingInvoke()
                lastVoiceInvoke = time
            }
        }

        override fun onVoiceRecorded(fileName: String) {
            callback.onVoiceRecorded(fileName)
        }

        override fun onVoiceError(error: String) {
            onVoiceVisibilityChanged(false)
        }

        override fun onAmplitudeChanged(amplitude: Float) {
            val newScale = 1 + amplitude * .7f
            val currentScale = rootView.vRecordIndicator.scaleX

            ObjectAnimator.ofPropertyValuesHolder(
                    rootView.vRecordIndicator,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, currentScale, newScale),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, currentScale, newScale)
            ).apply {
                interpolator = LinearInterpolator()
                duration = VoiceRecorder.AMPLITUDE_UPDATE_DELAY
                start()
            }
        }
    }

    private enum class KeyboardState {
        TEXT,
        STICKERS
    }
}