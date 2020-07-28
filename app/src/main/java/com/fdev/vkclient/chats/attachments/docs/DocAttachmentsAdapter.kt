package com.fdev.vkclient.chats.attachments.docs

import android.content.Context
import android.view.View
import com.fdev.vkclient.R
import com.fdev.vkclient.chats.attachments.base.BaseAttachmentsAdapter
import com.fdev.vkclient.model.attachments.Doc
import com.fdev.vkclient.utils.getSize
import com.fdev.vkclient.utils.stylize
import kotlinx.android.synthetic.main.item_attachments_doc.view.*

class DocAttachmentsAdapter(
        context: Context,
        loader: (Int) -> Unit,
        private val onClick: (Doc) -> Unit
) : BaseAttachmentsAdapter<Doc, DocAttachmentsAdapter.DocViewHolder>(context, loader) {

    override fun getViewHolder(view: View) = DocViewHolder(view)

    override fun getLayoutId() = R.layout.item_attachments_doc

    override fun createStubLoadItem() = Doc()

    inner class DocViewHolder(view: View) : BaseAttachmentViewHolder<Doc>(view) {

        override fun bind(item: Doc) {
            with(itemView) {
                tvExt.text = item.ext
                tvTitle.text = item.title
                tvSize.text = getSize(resources, item.size)
                relativeLayout.stylize(changeStroke = false)
                setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}