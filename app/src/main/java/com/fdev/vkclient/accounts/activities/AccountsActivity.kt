package com.fdev.vkclient.accounts.activities

import android.content.Context
import android.content.Intent
import com.fdev.vkclient.accounts.fragments.AccountsFragment
import com.fdev.vkclient.activities.ContentActivity
import com.fdev.vkclient.utils.launchActivity

class AccountsActivity : ContentActivity() {

    override fun createFragment(intent: Intent?) = AccountsFragment.newInstance()

    companion object {
        fun launch(context: Context?) {
            launchActivity(context, AccountsActivity::class.java)
        }
    }
}