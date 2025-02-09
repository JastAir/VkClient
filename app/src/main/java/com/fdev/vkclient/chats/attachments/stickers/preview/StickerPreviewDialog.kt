package com.fdev.vkclient.chats.attachments.stickers.preview

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.R
import com.fdev.vkclient.model.attachments.Sticker
import com.fdev.vkclient.utils.load
import com.fdev.vkclient.utils.stylize
import com.fdev.vkclient.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.dialog_sticker_preview.view.*

class StickerPreviewDialog(
        context: Context,
        private val sticker: Sticker,
        private val onKeywordsUpdated: (Int, List<String>) -> Unit
) : AlertDialog(context) {

    private val adapter by lazy {
        StickerKeywordsAdapter(context)
    }

    init {
        with(View.inflate(context, R.layout.dialog_sticker_preview, null)) {
            setView(this)
            ivSticker.load(sticker.photo512, placeholder = false)

            rvSuggestions.layoutManager = LinearLayoutManager(context)
            rvSuggestions.adapter = adapter
            adapter.addAll(sticker.keywords)

            rlAddKeyword.setOnClickListener {
                TextInputAlertDialog(context, "new keyword") { adapter.add(it) }.show()
            }
        }

        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok)) { _, _ ->
            saveChanges()
            dismiss()
        }
        setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel)) { _, _ ->
            dismiss()
        }
    }

    private fun saveChanges() {

    }

    override fun show() {
        super.show()
        stylize()
    }
}