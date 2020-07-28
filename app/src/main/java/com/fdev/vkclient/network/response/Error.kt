package com.fdev.vkclient.network.response

import com.google.gson.annotations.SerializedName
import com.fdev.vkclient.App
import com.fdev.vkclient.R


data class Error(

        @SerializedName("error_code")
        val code: Int = 0,
        @SerializedName("error_msg")
        val message: String? = null,
        @SerializedName("captcha_sid")
        val captchaSid: String? = null,
        @SerializedName("captcha_img")
        val captchaImg: String? = null
) {

    fun friendlyMessage() =
            when (code) {
                1 -> App.context.getString(R.string.error_1)
                5 -> App.context.getString(R.string.error_5)
                6 -> App.context.getString(R.string.error_6)
                7 -> App.context.getString(R.string.error_7)
                9 -> App.context.getString(R.string.error_9)
                10 -> App.context.getString(R.string.error_10)
                14 -> App.context.getString(R.string.error_14)
                15 -> App.context.getString(R.string.error_15)
                17 -> App.context.getString(R.string.error_17)
                200, 201, 203 -> App.context.getString(R.string.error_200plus)
                500, 600, 603 -> App.context.getString(R.string.error_500plus)
                else -> message
            }

    companion object {
        const val TOO_MANY = 6
        const val CAPTCHA = 14
    }
}