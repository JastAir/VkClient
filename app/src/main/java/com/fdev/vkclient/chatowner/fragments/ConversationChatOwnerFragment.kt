package com.fdev.vkclient.chatowner.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.R
import com.fdev.vkclient.chatowner.ChatOwnerActivity
import com.fdev.vkclient.chatowner.MembersAdapter
import com.fdev.vkclient.chats.messages.deepforwarded.DeepForwardedActivity
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.model.Conversation
import com.fdev.vkclient.model.User
import com.fdev.vkclient.utils.ColorManager
import com.fdev.vkclient.utils.setVisible
import com.fdev.vkclient.utils.showConfirm
import com.fdev.vkclient.utils.stylize
import com.fdev.vkclient.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.fragment_chat_owner_conversation.*

class ConversationChatOwnerFragment : BaseChatOwnerFragment<Conversation>() {

    private val adapter by lazy {
        MembersAdapter(context ?: return@lazy null, ::onUserClick, ::onUserLongClick)
    }

    override fun getLayoutId() = R.layout.fragment_chat_owner_conversation

    override fun getChatOwnerClass() = Conversation::class.java

    override fun bindChatOwner(chatOwner: Conversation?) {
        val conversation = chatOwner ?: return

        fabOpenChat.setVisible(conversation.canWrite?.allowed != false)
        addValue(R.drawable.ic_pinned, conversation.chatSettings?.pinnedMessage?.text, {
            conversation.chatSettings?.pinnedMessage?.id?.also { id ->
                DeepForwardedActivity.launch(context, id)
            }
        })
        viewModel.loadChatMembers(conversation.getPeerId())
        btnLeave.setOnClickListener { onLeaveGroupClick() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvUsers.layoutManager = LinearLayoutManager(context)
        rvUsers.adapter = adapter

        ivEdit.setOnClickListener { showTitleDialog() }
        ivEdit.stylize(ColorManager.LIGHT_TAG)
    }

    override fun getBottomPaddableView(): View = vBottom

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.conversationMembers.observe(viewLifecycleOwner, Observer(::onMembersLoaded))
    }

    private fun onMembersLoaded(profiles: List<User>) {
        adapter?.update(profiles)
    }

    private fun onUserClick(user: User) {
        ChatOwnerActivity.launch(context, user.id)
    }

    private fun onUserLongClick(user: User) {
        val peerId = getChatOwner()?.getPeerId() ?: 0
        var name = user.fullName
        if (Prefs.lowerTexts) {
            name = name.toLowerCase()
        }
        showConfirm(context, getString(R.string.wanna_kick_user, name)) { confirmed ->
            if (confirmed) {
                viewModel.kickUser(peerId, user.id)
            }
        }
    }

    private fun onLeaveGroupClick() {
        val peerId = getChatOwner()?.getPeerId() ?: 0
        showConfirm(context, getString(R.string.wanna_leave_conversation)) { confirmed ->
            if (confirmed) {
                viewModel.leaveConversation(peerId)
            }
        }
    }

    private fun showTitleDialog() {
        val context = context ?: return
        val chatOwner = getChatOwner() ?: return
        val oldTitle = chatOwner.getTitle()

        TextInputAlertDialog(context, getString(R.string.new_title), oldTitle) { newTitle ->
            viewModel.changeChatTitle(chatOwner.getPeerId(), newTitle)
        }.show()
    }

    companion object {
        fun newInstance(peerId: Int): ConversationChatOwnerFragment {
            val fragment = ConversationChatOwnerFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}