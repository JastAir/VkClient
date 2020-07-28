package com.fdev.vkclient.utils.contextpopup

data class ContextPopupItem(
        val iconRes: Int,
        val textRes: Int,
        val onClick: () -> Unit
)