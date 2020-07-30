package com.fdev.vkclient.features


import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.accounts.activities.AccountsActivity
import com.fdev.vkclient.accounts.models.Account
import com.fdev.vkclient.base.BaseFragment
import com.fdev.vkclient.chats.messages.chat.usual.ChatActivity
import com.fdev.vkclient.chats.messages.starred.StarredMessagesActivity
import com.fdev.vkclient.features.appearance.AppearanceActivity
import com.fdev.vkclient.features.assist.AssistActivity
import com.fdev.vkclient.features.general.GeneralActivity
import com.fdev.vkclient.features.notifications.NotificationsActivity
import com.fdev.vkclient.lg.LgAlertDialog
import com.fdev.vkclient.main.InsetViewModel
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.managers.Session
import com.fdev.vkclient.pin.PinActivity
import com.fdev.vkclient.utils.*
import com.fdev.vkclient.web.WebActivity
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_features.*
import javax.inject.Inject

class FeaturesFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: FeaturesViewModel.Factory
    private lateinit var viewModel: FeaturesViewModel

    private val insetViewModel by lazy {
        ViewModelProviders.of(activity ?: return@lazy null)[InsetViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.fragment_features

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[FeaturesViewModel::class.java]
        viewModel.getAccount().observe(this, Observer { updateAccount(it) })
        viewModel.loadAccount()

        rlAnalyse.setOnClickListener {
            firebaseSendEvent("features_analyse", "onClick")
            showToast(context, R.string.in_future_versions)
        }

        rlStarred.setOnClickListener {
            firebaseSendEvent("features_starred", "onClick")
            StarredMessagesActivity.launch(context)
        }

        rlAccounts.setOnClickListener {
            firebaseSendEvent("features_accounts", "onClick")
            AccountsActivity.launch(context)
        }

        rlGeneral.setOnClickListener {
            firebaseSendEvent("features_general", "onClick")
            GeneralActivity.launch(context)
            suggestJoin()
        }

        rlNotifications.setOnClickListener {
            firebaseSendEvent("features_notifications", "onClick")
            NotificationsActivity.launch(context)
            suggestJoin()
        }

        rlAppearance.setOnClickListener {
            firebaseSendEvent("features_appearance", "onClick")
            AppearanceActivity.launch(context)
            suggestJoin()
        }

        rlPin.setOnClickListener {
            firebaseSendEvent("features_onPin", "onClick")
            onPinClicked()
        }

        rlFeedback.setOnClickListener { ChatActivity.launch(context, -App.GROUP, getString(R.string.app_name)) }
        rlRate.setOnClickListener { context?.also { rate(it) } }
        rlContribute.setOnClickListener {
            firebaseSendEvent("features_contribute", "onClick")
            AssistActivity.launch(context)
        }
        rlShare.setOnClickListener {
            firebaseSendEvent("features_share", "onClick")
            share()
        }
        rlPrivacy.setOnClickListener {
            firebaseSendEvent("features_resolvePrivacyPolicy", "onClick")
            resolvePrivacyPolicy()
        }

        rlRoot.stylizeAll()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        updateTitle(getString(R.string.settings))
        insetViewModel?.topInset?.observe(viewLifecycleOwner, Observer { top ->
            rlAccounts.setTopMargin(top)
        })
        insetViewModel?.bottomInset?.observe(viewLifecycleOwner, Observer { bottom ->
            val bottomNavHeight = context?.resources?.getDimensionPixelSize(R.dimen.bottom_navigation_height) ?: 0
            svContent.setBottomPadding(bottom + bottomNavHeight)
        })
    }

    override fun onResume() {
        super.onResume()
        rlContribute.setVisible(time() - Prefs.lastAssistance > ASSISTANCE_DELAY)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun updateAccount(account: Account) {
        ivPhoto.load(account.photo)
        tvName.text = account.name
        if (Prefs.lowerTexts) tvName.lower()
    }

    private fun onPinClicked() {
        val context = context ?: return

        val pin = Prefs.pin
        if (TextUtils.isEmpty(pin)) {
            PinActivity.launch(context, PinActivity.Action.SET)
            suggestJoin()
        } else {
            val dialog = AlertDialog.Builder(context)
                    .setMessage(R.string.have_pin)
                    .setPositiveButton(R.string.edit) { _, _ ->
                        PinActivity.launch(context, PinActivity.Action.EDIT)
                    }
                    .setNegativeButton(R.string.reset) { _, _ ->
                        PinActivity.launch(context, PinActivity.Action.RESET)
                    }
                    .create()

            dialog.show()
            dialog.stylize()
        }
    }

    private fun showLogDialog() {
        LgAlertDialog(context ?: return).show()
    }

    private fun share() {
        viewModel.shareXvii({
            showToast(context, R.string.shared)
        }, { showError(context, it) })
    }

    private fun suggestJoin() {
        if (time() - Prefs.joinShownLast <= SHOW_JOIN_DELAY) return // one week

        Prefs.joinShownLast = time()
        if (!equalsDevUids(Session.uid)) {
            viewModel.checkMembership { inGroup ->
                if (!inGroup) {
                    val dialog = AlertDialog.Builder(context ?: return@checkMembership)
                            .setMessage(R.string.join_us)
                            .setPositiveButton(R.string.join) { _, _ -> viewModel.joinGroup() }
                            .setNegativeButton(R.string.cancel, null)
                            .create()
                    dialog.show()
                    dialog.stylize()
                }
            }
        }
    }

    private fun resolvePrivacyPolicy() {
        WebActivity.launch(context, PRIVACY_WORLD, getString(R.string.privacy_policy))
    }

    companion object {

        const val PRIVACY_WORLD = "https://github.com/JastAir/OpenFiles/blob/master/vkClientPolitics"

        const val SHOW_JOIN_DELAY = 3600 * 24 * 7 // one week

        const val ASSISTANCE_DELAY = 60 * 2 // two minutes

        fun newInstance() = FeaturesFragment()
    }
}