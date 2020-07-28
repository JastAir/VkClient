package com.fdev.vkclient.features.notifications.color

import androidx.annotation.StringRes

data class Color(
        val color: Int,

        @StringRes
        val titleRes: Int
)