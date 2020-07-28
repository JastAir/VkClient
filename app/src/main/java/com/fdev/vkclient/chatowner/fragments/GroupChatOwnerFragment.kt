package com.fdev.vkclient.chatowner.fragments

import android.os.Bundle
import android.view.View
import com.fdev.vkclient.R
import com.fdev.vkclient.model.Group
import kotlinx.android.synthetic.main.fragment_chat_owner_conversation.*

class GroupChatOwnerFragment : BaseChatOwnerFragment<Group>() {

    override fun getLayoutId() = R.layout.fragment_chat_owner_group

    override fun getChatOwnerClass() = Group::class.java

    override fun bindChatOwner(chatOwner: Group?) {
        val group = chatOwner ?: return

        addValue(R.drawable.ic_quotation, group.status)
        addValue(R.drawable.ic_sheet, group.description)
        addValue(R.drawable.ic_vk, group.screenName)
    }

    override fun getBottomPaddableView(): View = vBottom

    companion object {
        fun newInstance(peerId: Int): GroupChatOwnerFragment {
            val fragment = GroupChatOwnerFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}