package com.fdev.vkclient.chats.messages.chat.usual

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.fdev.vkclient.App
import com.fdev.vkclient.BuildConfig
import com.fdev.vkclient.R
import com.fdev.vkclient.chats.attachments.attachments.AttachmentsActivity
import com.fdev.vkclient.chats.messages.chat.base.BaseChatMessagesFragment
import com.fdev.vkclient.chats.messages.chat.secret.SecretChatActivity
import com.fdev.vkclient.dialogs.models.Dialog
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.model.attachments.Doc
import com.fdev.vkclient.utils.asText
import com.fdev.vkclient.utils.getTime
import com.fdev.vkclient.utils.matchesUserId
import com.fdev.vkclient.utils.time
import kotlinx.android.synthetic.main.chat_input_panel.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class ChatMessagesFragment : BaseChatMessagesFragment<ChatMessagesViewModel>() {

    override fun getViewModelClass() = ChatMessagesViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getMessageText()?.also { etInput.setText(it) }
    }

    override fun onPause() {
        super.onPause()
        viewModel.invalidateMessageText(etInput.asText())
    }

    override fun onEncryptedDocClicked(doc: Doc) {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_chat, menu)
        menu.findItem(R.id.menu_secret_chat)?.isVisible = peerId.matchesUserId()
        menu.findItem(R.id.menu_attach_logs)?.isVisible = peerId == -App.GROUP
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_attachments -> {
                AttachmentsActivity.launch(context, peerId)
                true
            }
            R.id.menu_secret_chat -> {
                SecretChatActivity.launch(context, peerId, title, photo)
                true
            }
            R.id.menu_attach_logs -> {
                val file = File(context?.cacheDir, "log_${BuildConfig.VERSION_NAME}_${getTime(time())}.txt")
                val writer = BufferedWriter(FileWriter(file))
                writer.write(Lg.getEvents())
                writer.close()
                onDocSelected(file.absolutePath)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        fun newInstance(dialog: Dialog, forwarded: String? = null,
                        shareText: String? = null, shareImage: String? = null): ChatMessagesFragment {
            val fragment = ChatMessagesFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, dialog.peerId)
                putString(ARG_TITLE, dialog.alias ?: dialog.title)
                putString(ARG_PHOTO, dialog.photo)
                if (!forwarded.isNullOrEmpty()) {
                    putString(ARG_FORWARDED, forwarded)
                }
                if (!shareText.isNullOrEmpty()) {
                    putString(ARG_SHARE_TEXT, shareText)
                }
                if (!shareImage.isNullOrEmpty()) {
                    putString(ARG_SHARE_IMAGE, shareImage)
                }
            }
            return fragment
        }
    }
}