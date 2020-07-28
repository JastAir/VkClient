package com.fdev.vkclient.views

import android.content.Context
import android.util.AttributeSet
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import com.fdev.vkclient.R
import com.fdev.vkclient.managers.Prefs

class XviiSwipeRefreshLayout : SwipyRefreshLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    init {
        setDistanceToTriggerSync(100)
        setProgressBackgroundColor(R.color.popup)
        setColorSchemeColors(Prefs.color)
    }

}