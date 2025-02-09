package com.fdev.vkclient.views

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.fdev.vkclient.R
import com.fdev.vkclient.utils.asText
import com.fdev.vkclient.utils.stylize
import kotlinx.android.synthetic.main.dialog_comment.view.*

class TextInputAlertDialog(
    context: Context,
    hint: String,
    presetText: String = "",
    private val onCommentAdded: (String) -> Unit
) : AlertDialog(context) {

    init {
        val view = View.inflate(context, R.layout.dialog_comment, null)
        with(view) {
            etComment.hint = hint
            etComment.setText(presetText)
        }
        setView(view)
        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok)) { _, _ ->
            onCommentAdded(view.etComment.asText())
            dismiss()
        }
    }

    override fun show() {
        super.show()
        stylize()
    }
}