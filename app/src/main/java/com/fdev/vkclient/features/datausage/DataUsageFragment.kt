package com.fdev.vkclient.features.datausage

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.fdev.vkclient.R
import com.fdev.vkclient.base.BaseFragment
import com.fdev.vkclient.network.datausage.DataUsageInterceptor
import com.fdev.vkclient.utils.getSize
import kotlinx.android.synthetic.main.fragment_data_usage.*

class DataUsageFragment : BaseFragment() {

    private val adapter by lazy {
        DataUsageAdapter(contextOrThrow)
    }

    override fun getLayoutId() = R.layout.fragment_data_usage

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val events = DataUsageInterceptor.events
        rvUsage.layoutManager = LinearLayoutManager(context)
        rvUsage.adapter = adapter
        adapter.addAll(events)

        context?.resources?.also {
            tvOutgoing.text = getSize(it, events.map { it.requestSize }.sum().toInt())
            tvIncoming.text = getSize(it, events.map { it.responseSize }.sum().toInt())
        }
    }

    companion object {
        fun newInstance() = DataUsageFragment()
    }
}