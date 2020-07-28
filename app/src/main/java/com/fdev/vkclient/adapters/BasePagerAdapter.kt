package com.fdev.vkclient.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * base PagerAdapter, the most common
 */
open class BasePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val fragments = ArrayList<Fragment>()
    private val titles = ArrayList<String>()

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size

    fun add(f: Fragment, title: String) {
        fragments.add(f)
        titles.add(title)
    }

    override fun getPageTitle(position: Int) = titles[position]
}