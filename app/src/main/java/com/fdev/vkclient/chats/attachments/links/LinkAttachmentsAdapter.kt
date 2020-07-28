package com.fdev.vkclient.chats.attachments.links

import android.content.Context
import android.view.View
import com.fdev.vkclient.R
import com.fdev.vkclient.chats.attachments.base.BaseAttachmentsAdapter
import com.fdev.vkclient.model.attachments.Link
import com.fdev.vkclient.utils.load
import kotlinx.android.synthetic.main.item_attachments_link.view.*

class LinkAttachmentsAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (Link) -> Unit
) : BaseAttachmentsAdapter<Link, LinkAttachmentsAdapter.LinkAttachmentsViewHolder>(context, loader) {

    override fun getViewHolder(view: View) = LinkAttachmentsViewHolder(view)

    override fun getLayoutId() = R.layout.item_attachments_link

    override fun createStubLoadItem() = Link()

    inner class LinkAttachmentsViewHolder(view: View)
        : BaseAttachmentsAdapter.BaseAttachmentViewHolder<Link>(view) {

        override fun bind(item: Link) {
            with(itemView) {
                tvTitle.text = item.title
                tvCaption.text = item.caption
                ivPhoto.load(item.photo?.getSmallPhoto()?.url)
                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}