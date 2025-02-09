package com.fdev.vkclient.pin

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.AndroidRuntimeException
import androidx.appcompat.app.AlertDialog
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.activities.BaseActivity
import com.fdev.vkclient.crypto.sha256
import com.fdev.vkclient.db.AppDb
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.managers.Session
import com.fdev.vkclient.utils.*
import com.fdev.vkclient.views.PinPadView
import kotlinx.android.synthetic.main.activity_pin.*
import javax.inject.Inject

/**
 * Created by root on 3/17/17.
 */

class PinActivity : BaseActivity() {

    @Inject
    lateinit var appDb: AppDb

    private val action by lazy {
        intent?.extras?.getSerializable(ACTION) as? Action
    }
    private var currentStage: Action? = null

    private var pin = ""
    private var confirmedPin = ""

    private var correctPin: String? = null
    private var failedPrompts: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent?.inject(this)
        setContentView(R.layout.activity_pin)

        action ?: finish()
        init()
        styleScreen(rlContainer)

//        AlarmActivity.launch(this)
    }

    private fun onPin(key: Int) {
        when (key) {
            PinPadView.DELETE -> {
                pin = ""
                tvPinDots.text = ""
            }

            PinPadView.OK -> onOkPressed()

            else -> {
                if (pin.length < LENGTH) {
                    pin += key
                    tvPinDots.text = "${tvPinDots.text}●"
                }
            }
        }
    }

    override fun getNavigationBarColor() = Color.TRANSPARENT

    private fun onOkPressed() {
        when (currentStage) {
            Action.ENTER -> if (isPinCorrect()) {
                onCorrect()
            } else {
                onIncorrect()
            }

            Action.SET -> {
                tvTitle.setText(R.string.confirm_pin)
                currentStage = Action.CONFIRM
                confirmedPin = pin
            }

            Action.CONFIRM -> if (pin == confirmedPin) {
                showToast(this, R.string.updated_succ)
                Prefs.pin = sha256("$pin$SALT")
                Session.pinLastPromptResult = time()
                finish()
            } else {
                currentStage = Action.SET
                tvTitle.setText(R.string.enter_new_pin)
                showError(this, R.string.dont_match)
            }
        }
        resetInput()
    }

    private fun onIncorrect() {
        failedPrompts++
        if (failedPrompts >= PROMPTS && action == Action.ENTER) {
            tvForgot.show()
        }
        showError(this, R.string.incorrect_pin)
    }

    private fun isPinCorrect() = correctPin == sha256("$pin$SALT")

    private fun onCorrect() {
        when (action) {

            Action.ENTER -> {
                Session.pinLastPromptResult = time()
                finish()
            }

            Action.RESET -> {
                Prefs.pin = ""
                showToast(this, R.string.reset_succ)
                finish()
            }

            Action.EDIT -> {
                tvTitle.setText(R.string.enter_new_pin)
                currentStage = Action.SET
            }
        }
    }

    private fun showResetDialog() {
        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.reset_pin)
                .setMessage(R.string.reset_pin_desc)
                .setPositiveButton(R.string.ok) { _, _ -> resetPin() }
                .setNegativeButton(R.string.cancel, null)
                .create()

        dialog.show()
        dialog.stylize()
    }

    private fun resetPin() {
        appDb.clearAsync()
        Session.token = ""
        Prefs.pin = ""
        restartApp(this, getString(R.string.restart_app))
    }

    private fun resetInput() {
        pin = ""
        tvPinDots.text = ""
    }

    private fun init() {
        pinPad.listener = { onPin(it) }
        tvForgot.setVisibleWithInvis(false)

        when (action) {
            Action.SET -> {
                tvTitle.setText(R.string.enter_new_pin)
                currentStage = Action.SET
            }

            Action.EDIT, Action.ENTER, Action.RESET -> {
                tvTitle.setText(R.string.enter_pin)
                correctPin = Prefs.pin
                currentStage = Action.ENTER
                tvForgot.setOnClickListener { showResetDialog() }
            }
        }
    }

    override fun onBackPressed() {
        if (action != Action.ENTER) {
            super.onBackPressed()
        }
    }

    /**
     * type of action pin is launched for
     */
    enum class Action {
        SET,
        ENTER,
        EDIT,
        RESET,
        CONFIRM
    }

    companion object {

        fun launch(context: Context?, action: Action) {
            context ?: return

            try {
                context.startActivity(Intent(context, PinActivity::class.java).apply {
                    putExtra(ACTION, action)
                })
            } catch (e: AndroidRuntimeException) {
                e.printStackTrace()
                Lg.wtf("error launching pin with $action: ${e.message}")
            }
        }

        private const val PROMPTS = 2

        const val ACTION = "action"

        private const val LENGTH = 8
        private const val SALT = "oi|6yw4-c5g846-d5c53s9mx"
    }
}
