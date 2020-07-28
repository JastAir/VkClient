package com.fdev.vkclient.chats.messages.chat.secret

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.chats.messages.chat.base.BaseChatMessagesFragment
import com.fdev.vkclient.dialogs.models.Dialog
import com.fdev.vkclient.managers.Session
import com.fdev.vkclient.model.attachments.Doc
import com.fdev.vkclient.photoviewer.ImageViewerActivity
import com.fdev.vkclient.utils.*
import com.fdev.vkclient.utils.contextpopup.ContextPopupItem
import com.fdev.vkclient.utils.contextpopup.createContextPopup
import com.fdev.vkclient.views.FingerPrintAlertDialog
import com.fdev.vkclient.views.LoadingDialog
import com.fdev.vkclient.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.chat_input_panel.*
import kotlinx.android.synthetic.main.fragment_chat.*

class SecretChatMessagesFragment : BaseChatMessagesFragment<SecretChatViewModel>() {

    override fun getViewModelClass() = SecretChatViewModel::class.java

    override fun inject() {
        App.appComponent?.inject(this)
    }

    override fun onEncryptedDocClicked(doc: Doc) {
        val dialogLoading = LoadingDialog(
                context ?: return,
                getString(R.string.decrypting_image)
        )
        dialogLoading.show()
        viewModel.decryptDoc(doc) { verified, path ->
            dialogLoading.dismiss()
            if (!path.isNullOrEmpty() && verified) {
                ImageViewerActivity.viewImage(context, "file://$path")
            } else {
                showError(context, R.string.invalid_file)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rlNoKeys.setVisible(viewModel.isKeyRequired())
        if (viewModel.isKeyRequired()) {
            showKeysDialog()
        }
        rlNoKeys.setOnClickListener {
            showKeysDialog()
        }
        ivKeyPattern.show()

        viewModel.getKeysSet().observe(this, Observer {
            rlNoKeys.setVisible(!it)
            viewModel.loadMessages()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_secret_chat, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item?.itemId) {
        R.id.menu_fingerprint -> {
            if (viewModel.isKeyRequired()) {
                showKeysDialog()
            } else {
                val fingerprint = viewModel.getFingerprint()
                val keyType = viewModel.getKeyType()
                context?.let {
                    FingerPrintAlertDialog(it, fingerprint, keyType).show()
                }
            }
            true
        }
        R.id.menu_keys -> {
            showKeysDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showKeysDialog() {
        val canMakeExchange = peerId.matchesUserId() && peerId != Session.uid
        if (canMakeExchange) {
            createContextPopup(context ?: return, arrayListOf(
                    ContextPopupItem(R.drawable.ic_edit_popup, R.string.user_key) {
                        showKeyInputDialog()
                    },
                    ContextPopupItem(R.drawable.ic_key_exchange, R.string.random_key) {
                        showAlert(context, getString(R.string.generation_dh_hint)) {
                            viewModel.startExchange()
                        }
                    })).show()
        } else {
            showKeyInputDialog()
        }
    }

    private fun showKeyInputDialog() {
        TextInputAlertDialog(
                context ?: return,
                getString(R.string.user_key), "") { userKey ->
            if (userKey.isEmpty()) {
                showError(context, R.string.empty_user_key)
            } else {
                viewModel.setKey(userKey)
                showToast(activity, getString(R.string.key_set))
                rlNoKeys.hide()
                viewModel.loadMessages()
            }
        }.show()
    }

    companion object {

        fun newInstance(dialog: Dialog): SecretChatMessagesFragment {
            val fragment = SecretChatMessagesFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, dialog.peerId)
                putString(ARG_TITLE, dialog.alias ?: dialog.title)
                putString(ARG_PHOTO, dialog.photo)
            }
            return fragment
        }
    }
}