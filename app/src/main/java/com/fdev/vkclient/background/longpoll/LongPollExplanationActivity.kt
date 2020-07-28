package com.fdev.vkclient.background.longpoll

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.fdev.vkclient.R
import com.fdev.vkclient.activities.BaseActivity
import com.fdev.vkclient.utils.setBottomInsetPadding
import com.fdev.vkclient.utils.setTopInsetPadding
import kotlinx.android.synthetic.main.activity_explanation.*

class LongPollExplanationActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explanation)
        tvTitle.setTopInsetPadding()
        svContent.setBottomInsetPadding()
    }

    override fun getStatusBarColor() = ContextCompat.getColor(this, R.color.status_bar)
}