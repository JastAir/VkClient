package com.fdev.vkclient.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.fdev.vkclient.dialogs.fragments.DialogsFragment
import com.fdev.vkclient.features.FeaturesFragment
import com.fdev.vkclient.friends.fragments.FriendsFragment
import com.fdev.vkclient.profile.fragments.ProfileFragment
import com.fdev.vkclient.search.SearchFragment

class MainPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragments = arrayListOf<Fragment>()

    init {
        fragments.apply {
            add(SearchFragment.newInstance())
            add(DialogsFragment.newInstance())
            add(FriendsFragment.newInstance())
            add(ProfileFragment.newInstance())
        }
    }

    override fun getCount() = fragments.size

    override fun getItem(position: Int) = fragments[position]
}