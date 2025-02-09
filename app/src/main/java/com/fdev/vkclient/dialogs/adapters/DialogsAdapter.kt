package com.fdev.vkclient.dialogs.adapters

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fdev.vkclient.R
import com.fdev.vkclient.base.BaseReachAdapter
import com.fdev.vkclient.dialogs.models.Dialog
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.utils.*
import kotlinx.android.synthetic.main.item_dialog.view.*

class DialogsAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (Dialog) -> Unit,
        private val onLongClick: (Dialog) -> Unit
) : BaseReachAdapter<Dialog, DialogsAdapter.DialogViewHolder>(context, loader) {

    var typingPeerIds: Set<Int> = emptySet()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var firstItemPadding = 0

    override fun createStubLoadItem() = Dialog()

    override fun createHolder(parent: ViewGroup, viewType: Int)
            = DialogViewHolder(inflater.inflate(R.layout.item_dialog, null))

    override fun bind(holder: DialogViewHolder, item: Dialog) {
        holder.bind(item, items[0] == item)
    }

    inner class DialogViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(dialog: Dialog, isFirst: Boolean) {
            with(itemView) {
                val topPadding = if (isFirst) firstItemPadding else 0
                setPadding(0, topPadding, 0, 0)

                civPhoto.load(dialog.photo)

                tvTitle.text = dialog.alias ?: dialog.title
                if (Prefs.lowerTexts) tvTitle.lower()
                tvBody.text = if (EmojiHelper.hasEmojis(dialog.text)) {
                    EmojiHelper.getEmojied(
                            context,
                            dialog.text,
                            Html.fromHtml(getMessageBody(context, dialog)) as SpannableStringBuilder
                    )
                } else {
                    Html.fromHtml(getMessageBody(context, dialog))
                }
                typingView.stylize()

                val isTyping = dialog.peerId in typingPeerIds
                typingView.setVisible(isTyping)
                tvYou.setVisibleWithInvis(!isTyping)
                tvBody.setVisibleWithInvis(!isTyping)

                tvDate.text = getTime(dialog.timeStamp, shortened = true, withSeconds = Prefs.showSeconds)

                ivMute.setVisible(dialog.isMute)
                tvYou.setVisible(dialog.isOut && !isTyping)
                ivPinned.setVisible(dialog.isPinned)
                ivOnlineDot.setVisible(dialog.isOnline)
                ivUnreadDotOut.setVisible(!dialog.isRead && dialog.isOut)
                rlUnreadCount.setVisible(!dialog.isRead && !dialog.isOut && dialog.unreadCount > 0)

                if (dialog.unreadCount != 0) {
                    val unread = if (dialog.unreadCount > 99) {
                        context.getString(R.string.unread100)
                    } else {
                        dialog.unreadCount.toString()
                    }
                    tvUnreadCount.text = unread
                }

                ivOnlineDot.stylize(ColorManager.MAIN_TAG)
                ivUnreadDotOut.stylize(ColorManager.MAIN_TAG, changeStroke = false)
                rlUnreadCount.stylize()
                rlItemContainer.setOnClickListener {
                    items.getOrNull(adapterPosition)?.also(onClick)
                }
                rlItemContainer.setOnLongClickListener {
                    items.getOrNull(adapterPosition)?.also(onLongClick)
                    true
                }
            }
        }

        private fun getMessageBody(context: Context, dialog: Dialog): String {
            if (dialog.text.isNotEmpty()) {
                return wrapMentions(context, dialog.text).toString()
            }
            return context.getString(R.string.error_message)
        }
    }
}