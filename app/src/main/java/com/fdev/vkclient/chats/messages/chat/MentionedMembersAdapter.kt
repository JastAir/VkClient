package com.fdev.vkclient.chats.messages.chat

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fdev.vkclient.R
import com.fdev.vkclient.adapters.BaseAdapter
import com.fdev.vkclient.model.User
import com.fdev.vkclient.utils.load
import kotlinx.android.synthetic.main.item_user_mentioned.view.*

class MentionedMembersAdapter(
        context: Context,
        private val onClick: (User) -> Unit
) : BaseAdapter<User, MentionedMembersAdapter.MemberViewHolder>(context) {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ) = MemberViewHolder(inflater.inflate(R.layout.item_user_mentioned, parent, false))

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(member: User) {
            with(itemView) {
                tvName.text = member.fullName
                tvInfo.text = "@${member.getPageName()}"
                civPhoto.load(member.photo100)

                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}