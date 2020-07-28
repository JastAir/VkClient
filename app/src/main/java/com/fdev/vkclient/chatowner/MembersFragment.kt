package com.fdev.vkclient.chatowner

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fdev.vkclient.R
import com.fdev.vkclient.adapters.BaseAdapter
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.model.User
import com.fdev.vkclient.utils.*
import kotlinx.android.synthetic.main.item_user.view.*

class MembersAdapter(
        context: Context,
        private val onClick: (User) -> Unit,
        private val onLongClick: (User) -> Unit
) : BaseAdapter<User, MembersAdapter.MemberViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MemberViewHolder(inflater.inflate(R.layout.item_user, parent, false))

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class MemberViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(user: User) {
            with(view) {
                civPhoto.load(user.photo100)
                val d = ContextCompat.getDrawable(context, R.drawable.dotshape)
                d?.stylize(ColorManager.MAIN_TAG)
                ivOnlineDot.setImageDrawable(if (user.isOnline) d else null)
                tvName.text = user.fullName
                if (Prefs.lowerTexts) {
                    tvName.lower()
                }

                user.lastSeen?.also {
                    tvInfo.text = getLastSeenText(resources, user.isOnline, it.time, it.platform)
                }
                setOnClickListener { onClick(items[adapterPosition]) }
                setOnLongClickListener { onLongClick(items[adapterPosition]); true }
            }
        }
    }

}