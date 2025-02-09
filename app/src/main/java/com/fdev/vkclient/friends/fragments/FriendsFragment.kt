package com.fdev.vkclient.friends.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.base.BaseFragment
import com.fdev.vkclient.chatowner.ChatOwnerActivity
import com.fdev.vkclient.friends.adapters.FriendsAdapter
import com.fdev.vkclient.friends.viewmodel.FriendsViewModel
import com.fdev.vkclient.main.InsetViewModel
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.Wrapper
import com.fdev.vkclient.search.SearchActivity
import com.fdev.vkclient.utils.*
import kotlinx.android.synthetic.main.fragment_friends.*
import javax.inject.Inject

class FriendsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: FriendsViewModel.Factory
    private lateinit var viewModel: FriendsViewModel

    private val adapter by lazy {
        FriendsAdapter(contextOrThrow, ::onClick, ::loadMore)
    }

    private val insetViewModel by lazy {
        ViewModelProviders.of(activity ?: return@lazy null)[InsetViewModel::class.java]
    }

    override fun getLayoutId() = R.layout.fragment_friends

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[FriendsViewModel::class.java]
        viewModel.getFriends().observe(this, Observer { updateFriends(it) })
        viewModel.loadFriends()
        adapter.startLoading()

        progressBar.show()
        rvFriends.layoutManager = LinearLayoutManager(context)
        rvFriends.adapter = adapter

        progressBar.stylize()
        swipeRefresh.setOnRefreshListener {
            viewModel.loadFriends()
            adapter.reset()
            adapter.startLoading()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        insetViewModel?.topInset?.observe(viewLifecycleOwner, Observer { top ->
            adapter.firstItemPadding = top
        })
        insetViewModel?.bottomInset?.observe(viewLifecycleOwner, Observer { bottom ->
            val bottomNavHeight = context?.resources?.getDimensionPixelSize(R.dimen.bottom_navigation_height) ?: 0
            rvFriends.setBottomPadding(bottom + bottomNavHeight)
        })
    }

    private fun updateFriends(data: Wrapper<ArrayList<User>>) {
        swipeRefresh.isRefreshing = false
        progressBar.hide()
        if (data.data != null) {
            adapter.update(data.data)
        } else {
            showError(context, data.error ?: getString(R.string.error))
        }
    }

    private fun loadMore(offset: Int) {
        viewModel.loadFriends(offset)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.user_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                SearchActivity.launch(context)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onClick(user: User) {
        ChatOwnerActivity.launch(context, user.id)
    }

    companion object {
        fun newInstance() = FriendsFragment()
    }
}