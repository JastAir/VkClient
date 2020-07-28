package com.fdev.vkclient.views

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.fdev.vkclient.R
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.utils.rate
import com.fdev.vkclient.utils.stylize
import kotlinx.android.synthetic.main.dialog_rate.view.*

class RateAlertDialog(context: Context) : AlertDialog(context) {

    init {
        val view = View.inflate(context, R.layout.dialog_rate, null)
        with(view) {
            tvRate.setOnClickListener {
                rate(context)
                Prefs.showRate = false
                dismiss()
            }
            tvNotNow.setOnClickListener {
                dismiss()
            }
            tvNever.setOnClickListener {
                Prefs.showRate = false
                dismiss()
            }
        }
        setView(view)
    }

    override fun show() {
        if (Prefs.showRate) {
            super.show()
            stylize()
        }
    }
}