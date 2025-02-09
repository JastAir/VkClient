package com.fdev.vkclient.analyzer.dialog

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.base.BaseFragment
import com.fdev.vkclient.model.User
import com.fdev.vkclient.model.Wrapper
import kotlinx.android.synthetic.main.fragment_analyse_dialog_all.*
import javax.inject.Inject

class AnalyzeDialogFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: AnalyseDialogViewModel.Factory
    private lateinit var viewModel: AnalyseDialogViewModel

    private val peerId by lazy { arguments?.getInt(ARG_PEER_ID) ?: 0 }

    override fun getLayoutId() = R.layout.fragment_analyse_dialog_all

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.appComponent?.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)[AnalyseDialogViewModel::class.java]
        viewModel.peerId = peerId
        viewModel.getUser().observe(this, Observer { updateUser(it) })
        viewModel.getProgress().observe(this, Observer { updateProgress(it) })
        viewModel.analyse()
    }

    private fun updateUser(data: Wrapper<User>) {
        val text = tvData.text.toString()
        tvData.text = text + "\n" + data.data
    }

    private fun updateProgress(data: Wrapper<Pair<Int, Int>>) {
        val text = tvData.text.toString()
        if (data.data != null) {
            tvData.text = text + "\n" + data.data.first + "/" + data.data.second
        }
    }

    companion object {

        const val ARG_PEER_ID = "peerId"

        fun newInstance(peerId: Int): AnalyzeDialogFragment {
            val fragment = AnalyzeDialogFragment()
            fragment.arguments = Bundle().apply {
                putInt(ARG_PEER_ID, peerId)
            }
            return fragment
        }
    }
}