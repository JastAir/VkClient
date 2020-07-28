package com.fdev.vkclient.search

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fdev.vkclient.R
import com.fdev.vkclient.adapters.BaseAdapter
import com.fdev.vkclient.dialogs.models.Dialog
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.utils.*
import kotlinx.android.synthetic.main.item_dialog_search.view.*

class SearchAdapter(
        context: Context,
        private val onClick: (Dialog) -> Unit
) : BaseAdapter<Dialog, SearchAdapter.SearchViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            = SearchViewHolder(inflater.inflate(R.layout.item_dialog_search, null))

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(dialog: Dialog) {
            with(itemView) {
                civPhoto.load(dialog.photo)
                tvTitle.text = dialog.title
                if (Prefs.lowerTexts) {
                    tvTitle.lower()
                }
                ivOnlineDot.hide() // due to this list is not autorefreshable

                ivOnlineDot.stylize(ColorManager.MAIN_TAG)
                rlItemContainer.setOnClickListener { onClick(items[adapterPosition]) }
            }
        }
    }
}