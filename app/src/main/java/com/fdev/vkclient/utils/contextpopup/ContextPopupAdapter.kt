package com.fdev.vkclient.utils.contextpopup

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.fdev.vkclient.R
import com.fdev.vkclient.adapters.BaseAdapter
import com.fdev.vkclient.utils.stylize
import kotlinx.android.synthetic.main.item_context_popup.view.*

class ContextPopupAdapter(
        context: Context,
        private val dialog: AlertDialog
) : BaseAdapter<ContextPopupItem, ContextPopupAdapter.ContextPopupItemHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ContextPopupItemHolder(inflater.inflate(R.layout.item_context_popup, parent, false))

    override fun onBindViewHolder(holder: ContextPopupItemHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ContextPopupItemHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: ContextPopupItem) {
            with(itemView) {
                tvTitle.text = context.getString(item.textRes)
                ivIcon.setImageResource(item.iconRes)
                ivIcon.stylize()
                rlBack.setOnClickListener {
                    dialog.dismiss()
                    item.onClick()
                }
            }
        }
    }
}