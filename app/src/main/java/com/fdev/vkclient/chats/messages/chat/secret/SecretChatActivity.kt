package com.fdev.vkclient.chats.messages.chat.secret

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.fdev.vkclient.R
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.dialogs.models.Dialog
import com.fdev.vkclient.model.User

class SecretChatActivity : ContentActivity() {

    override fun getLayoutId() = R.layout.activity_content

    override fun onResume() {
        super.onResume()
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun createFragment(intent: Intent?): Fragment {
        val args = intent?.extras
        val dialog = args?.getParcelable(DIALOG) ?: Dialog()
        return SecretChatMessagesFragment.newInstance(dialog)
    }

    override fun getNavigationBarColor() = Color.TRANSPARENT

    companion object {
        const val DIALOG = "dialog"

        fun launch(context: Context?, userId: Int, title: String,
                   avatar: String? = null) {
            launch(context, Dialog(
                    peerId = userId,
                    title = title,
                    photo = avatar
            ))

        }

        fun launch(context: Context?, dialog: Dialog) {
            context ?: return

            context.startActivity(Intent(context, SecretChatActivity::class.java).apply {
                putExtra(DIALOG, dialog)
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }

        fun launch(context: Context?, user: User) {
            launch(context, Dialog(
                    peerId = user.id,
                    title = user.fullName,
                    photo = user.photo100
            ))
        }
    }
}