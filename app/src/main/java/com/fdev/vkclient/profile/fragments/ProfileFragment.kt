package com.fdev.vkclient.profile.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.base.BaseFragment
import com.fdev.vkclient.chats.messages.chat.usual.ChatActivity
import com.fdev.vkclient.features.FeaturesActivity
import com.fdev.vkclient.features.FeaturesFragment
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.managers.Session
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.model.attachments.Photo
import com.fdev.vkclient.photoviewer.ImageViewerActivity
import com.fdev.vkclient.profile.viewmodels.ProfileViewModel
import com.fdev.vkclient.utils.*
import com.fdev.vkclient.views.RateAlertDialog
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.item_user_field.view.*
import javax.inject.Inject

class ProfileFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ProfileViewModel.Factory
    private lateinit var viewModel: ProfileViewModel

    private val userId by lazy { arguments?.getInt(ARG_USER_ID) ?: 0 }
    private val userIsOwner by lazy { arguments?.getBoolean(ARG_USER_TYPE) ?: false }

    override fun getLayoutId() = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stylize()
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[ProfileViewModel::class.java]
        viewModel.getUser().observe(this, Observer { updateUser(it) })
        viewModel.getFoaf().observe(this, Observer { updateFoaf(it) })
        viewModel.userId = userId
        viewModel.loadUser()

        context?.let { RateAlertDialog(it).show() }
    }

    private fun stylize() {
        if (Prefs.isLightTheme) {
//            rlBack.stylizeColor()
            llHeader.setBackgroundColor(Color.WHITE)
            llContainer.setBackgroundColor(Color.WHITE)
            llCounters.stylize()
            rlChat.stylize()
        }
    }

    private fun updateUser(data: Wrapper<User>) {
        progressBar.hide()
        if (data.data != null) {
            bindUser(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun onPhotosLoaded(photos: ArrayList<Photo>) {
        ImageViewerActivity.viewImages(context, photos)
    }

    private fun updateFoaf(data: Wrapper<String>) {
        if (data.data != null) {
            add(R.string.registration_date, formatDate(data.data).toLowerCase())
        }
    }

    private fun bindUser(user: User) {
        llContainer.removeAllViews()
        civPhoto.load(user.photoMax)
        civPhoto.setOnClickListener { viewModel.loadPhotos(::onPhotosLoaded) }
        flSettings.setOnClickListener { FeaturesActivity.launch(context) }

        tvName.text = user.fullName
        if (Prefs.lowerTexts) tvName.lower()
        rlChat.setOnClickListener { ChatActivity.launch(context, user) }
        if (!user.deactivated.isNullOrEmpty()) return

        user.lastSeen?.also {
            tvLastSeen.text = getLastSeenText(context?.resources, user.isOnline, it.time, it.platform)
        }

        add(R.string.link, user.link, { goTo(user.link) }) { copy(user.link, R.string.link) }
        add(R.string.id, "${user.id}", null) { copy("${user.id}", R.string.id) }
        add(R.string.status, user.status, null) { copy(user.status, R.string.status) }
        add(R.string.bdate, formatDate(formatBdate(user.bdate)).toLowerCase())
        if (user.city != null) {
            add(R.string.city, user.city.title)
        }
        add(R.string.hometown, user.hometown)
        add(R.string.relation, getRelation(context, user.relation))
        add(R.string.mphone, user.mobilePhone,
                {
                    callIntent(context, user.mobilePhone)
                }) { copy(user.mobilePhone, R.string.mphone) }
        add(R.string.hphone, user.homePhone,
                {
                    callIntent(context, user.homePhone)
                }) { copy(user.homePhone, R.string.hphone) }
        add(R.string.facebook, user.facebook, null) { copy(user.facebook, R.string.facebook) }
        add(R.string.site, user.site, { goTo(user.site) }) { copy(user.site, R.string.site) }
        add(R.string.twitter, user.twitter, { goTo("https://twitter.com/${user.instagram}") }) { copy(user.twitter, R.string.twitter) }
        add(R.string.instagram, user.instagram, { goTo("https://instagram.com/${user.instagram}") }) { copy(user.instagram, R.string.instagram) }
        add(R.string.skype, user.skype, null) { copy(user.skype, R.string.skype) }
        tvFriendsCOunt.text = shortifyNumber(user.counters?.friends ?: 0)
        tvFollowersCount.text = shortifyNumber(user.counters?.followers ?: 0)
    }

    private fun copy(text: String?, title: Int) {
        copyToClip(text ?: return)
        showToast(activity, getString(R.string.copied, getString(title)))
    }

    private fun goTo(url: String?) {
        simpleUrlIntent(context, url)
    }

    private fun add(@StringRes title: Int,
                    value: String?,
                    onClick: ((View) -> Unit)? = null,
                    onLongClick: ((View) -> Unit)? = null) {
        if (!value.isNullOrEmpty()) {
            val view = View.inflate(activity, R.layout.item_user_field, null)
            with(view) {
                tvTitle.text = getString(title)
                tvValue.text = value
                rlItem.setOnClickListener(onClick)
                rlItem.setOnLongClickListener {
                    onLongClick?.invoke(it)
                    true
                }
            }
            llContainer.addView(view)
        }
    }

    companion object {

        const val ARG_USER_ID = "userId"
        const val ARG_USER_TYPE = "userType"

        fun newInstance(userId: Int? = null): ProfileFragment {

            val fragment = ProfileFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_USER_ID, userId ?: Session.uid)
                putBoolean(ARG_USER_TYPE, userId == null)
            }
            return fragment
        }

    }
}