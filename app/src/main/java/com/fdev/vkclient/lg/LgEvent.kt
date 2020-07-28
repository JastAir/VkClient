package com.fdev.vkclient.lg

import android.os.Parcelable
import com.fdev.vkclient.utils.time
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LgEvent(
        val text: String,
        val type: Type = Type.INFO,
        val ts: Int = time()
) : Parcelable {

    enum class Type {
        INFO,
        ERROR
    }
}