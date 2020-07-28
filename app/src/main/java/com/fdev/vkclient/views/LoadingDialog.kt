package com.fdev.vkclient.views

import android.app.AlertDialog
import android.content.Context
import android.view.View
import com.fdev.vkclient.R
import com.fdev.vkclient.utils.hide
import kotlinx.android.synthetic.main.dialog_loading.view.*

class LoadingDialog(context: Context,
                    text: String = "",
                    cancelable: Boolean = false) : AlertDialog(context) {

    init {
        val view = View.inflate(context, R.layout.dialog_loading, null)
        with(view) {
            if (text.isNotEmpty()) {
                tvTitle.text = text
            } else {
                tvTitle.hide()
            }
        }
        setCancelable(cancelable)
        setView(view)
    }

}