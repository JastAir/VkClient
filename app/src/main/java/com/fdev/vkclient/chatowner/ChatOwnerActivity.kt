package com.fdev.vkclient.chatowner

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fdev.vkclient.R
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.chatowner.fragments.BaseChatOwnerFragment
import com.fdev.vkclient.chatowner.fragments.ConversationChatOwnerFragment
import com.fdev.vkclient.chatowner.fragments.GroupChatOwnerFragment
import com.fdev.vkclient.chatowner.fragments.UserChatOwnerFragment
import com.fdev.vkclient.utils.matchesChatId
import com.fdev.vkclient.utils.matchesGroupId


class ChatOwnerActivity : ContentActivity() {

    override fun createFragment(intent: Intent?): Fragment {
        val peerId = resolvePeerId()

        return when {
            peerId.matchesChatId() -> ConversationChatOwnerFragment.newInstance(peerId)
            peerId.matchesGroupId() -> GroupChatOwnerFragment.newInstance(peerId)
            else -> UserChatOwnerFragment.newInstance(peerId)
        }
    }

    override fun getNavigationBarColor() = ContextCompat.getColor(this, R.color.navigation_bar_chat_owner)

    private fun resolvePeerId(): Int {
        if (intent.action == Intent.ACTION_VIEW) {
            intent?.data?.lastPathSegment?.also { path ->
                return try {
                    Integer.parseInt(path.replace("id", ""))
                } catch (e: NumberFormatException) {
                    0
                }
            }
            return 0
        } else {
            return intent?.extras?.getInt(BaseChatOwnerFragment.ARG_PEER_ID) ?: 0
        }
    }


    companion object {

        fun launch(context: Context?, peerId: Int) {
            context?.startActivity(Intent(context, ChatOwnerActivity::class.java).apply {
                putExtra(BaseChatOwnerFragment.ARG_PEER_ID, peerId)
            })
        }
    }
}