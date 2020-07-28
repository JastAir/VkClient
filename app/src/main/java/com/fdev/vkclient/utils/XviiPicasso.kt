package com.fdev.vkclient.utils

import android.annotation.SuppressLint
import com.squareup.picasso.Picasso
import com.fdev.vkclient.App

object XviiPicasso {

    @SuppressLint("StaticFieldLeak")
    private var instance: Picasso? = null

    fun get(): Picasso = instance ?: synchronized(this) {
        instance ?: create().also {
            instance = it
        }
    }

    private fun create() = Picasso.Builder(App.context)
            // not convenient and not applicable
//            .downloader(OkHttp3Downloader(
//                    OkHttpClient.Builder()
//                            .addInterceptor(DataUsageInterceptor(DataUsageEvent.Type.PHOTO))
//                            .build()
//            ))
            .build()

}