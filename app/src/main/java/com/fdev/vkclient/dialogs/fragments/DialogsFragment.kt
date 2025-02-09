package com.fdev.vkclient.dialogs.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.base.BaseFragment
import com.fdev.vkclient.chats.messages.chat.secret.SecretChatActivity
import com.fdev.vkclient.chats.messages.chat.usual.ChatActivity
import com.fdev.vkclient.dialogs.adapters.DialogsAdapter
import com.fdev.vkclient.dialogs.models.Dialog
import com.fdev.vkclient.dialogs.viewmodels.DialogsViewModel
import com.fdev.vkclient.main.InsetViewModel
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.utils.*
import com.fdev.vkclient.utils.contextpopup.ContextPopupItem
import com.fdev.vkclient.utils.contextpopup.createContextPopup
import com.fdev.vkclient.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.activity_root.view.*
import kotlinx.android.synthetic.main.fragment_dialogs.*
import kotlinx.android.synthetic.main.toolbar.*
import javax.inject.Inject


open class DialogsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: DialogsViewModel.Factory
    private lateinit var viewModel: DialogsViewModel

    private val adapter by lazy {
        DialogsAdapter(contextOrThrow, ::loadMore, ::onClick, ::onLongClick)
    }

    private val insetViewModel by lazy {
        ViewModelProviders.of(activity ?: return@lazy null)[InsetViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.fragment_dialogs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()

        progressBar.show()
        swipeRefresh.setOnRefreshListener {
            viewModel.loadDialogs()
            adapter.reset()
            adapter.startLoading()
        }
        progressBar.stylize()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[DialogsViewModel::class.java]
        viewModel.getDialogs().observe(viewLifecycleOwner, Observer(::updateDialogs))
        viewModel.getTypingPeerIds().observe(viewLifecycleOwner, Observer { adapter.typingPeerIds = it })
        viewModel.loadDialogs()
        adapter.startLoading()

        insetViewModel?.topInset?.observe(viewLifecycleOwner, Observer { top ->
            adapter.firstItemPadding = top
        })
        insetViewModel?.bottomInset?.observe(viewLifecycleOwner, Observer { bottom ->
            val bottomNavHeight = context?.resources?.getDimensionPixelSize(R.dimen.bottom_navigation_height) ?: 0
            rvDialogs.setBottomPadding(bottom + bottomNavHeight)
        })
    }

    private fun initRecycler() {
        rvDialogs.layoutManager = LinearLayoutManager(context)
        rvDialogs.adapter = adapter
    }

    private fun updateDialogs(data: Wrapper<ArrayList<Dialog>>) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            adapter.update(data.data)
//            adapter.update(FakeData.dialogs)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        viewModel.loadDialogs(offset)
    }

    protected open fun onClick(dialog: Dialog) {
        ChatActivity.launch(context, dialog)
    }

    protected open fun onLongClick(dialog: Dialog) {
        val items = arrayListOf(
                ContextPopupItem(R.drawable.ic_pinned, if (dialog.isPinned) R.string.unpin else R.string.pin) {
                    viewModel.pinDialog(dialog)
                },
                ContextPopupItem(R.drawable.ic_eye, R.string.mark_as_read) {
                    viewModel.readDialog(dialog)
                },
                ContextPopupItem(R.drawable.ic_delete_popup, R.string.delete) {
                    showDeleteDialog(context) {
                        viewModel.deleteDialog(dialog)
                    }
                },
                ContextPopupItem(R.drawable.ic_alias, R.string.alias) {
                    TextInputAlertDialog(
                            contextOrThrow,
                            dialog.title,
                            dialog.alias ?: dialog.title
                    ) { newAlias ->
                        viewModel.addAlias(dialog, newAlias)
                    }.show()
                },
                ContextPopupItem(R.drawable.ic_home, R.string.add_shortcut) {
                    createShortcut(context, dialog)
                }
        )

        if (dialog.peerId.matchesUserId()) {
            items.add(ContextPopupItem(R.drawable.ic_start_secret_chat, R.string.encryption) {
                SecretChatActivity.launch(context, dialog)
            })
        }

        createContextPopup(context ?: return, items).show()
    }

    companion object {
        fun newInstance() = DialogsFragment()
    }
}