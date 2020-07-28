package com.fdev.vkclient

import android.app.Application
import android.content.Context
import com.fdev.vkclient.crypto.KeyHolder
import com.fdev.vkclient.dagger.AppComponent
import com.fdev.vkclient.dagger.DaggerAppComponent
import com.fdev.vkclient.dagger.modules.ContextModule
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.utils.AppLifecycleTracker
import com.fdev.vkclient.utils.ColorManager
import com.fdev.vkclient.utils.EmojiHelper
import com.fdev.vkclient.utils.StatTool
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(this))
                .build()

        registerActivityLifecycleCallbacks(AppLifecycleTracker())
        ColorManager.init(applicationContext)
        KeyHolder.reinit()
        EmojiHelper.init()

        CalligraphyConfig.initDefault(
                CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Rubik-Light.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        )

        try {
            StatTool.init(applicationContext)
        } catch (e: Exception) {
            Lg.wtf("[stat] init failed")
        }
    }

    companion object {
        lateinit var context: Context
        var appComponent: AppComponent? = null

        const val VERSION = "5.63"
        const val APP_ID = 7551761
        const val SCOPE_ALL = 471062
        const val REDIRECT_URL = "https://oauth.vk.com/blank.html"
        const val API_URL = "https://api.vk.com/method/"
        const val SHARE_POST = "wall-137238289_316"

        val ID_HASHES = arrayListOf("260ca2827e258c06153e86d121de1094", "44b8e44538545051a8bd710e5e10e5ce", "7c3785059f7ffd4a21d38bd203d13721")
        val ID_SALTS = arrayListOf("iw363c8b6385cy4", "iw57xs57fdvb4en", "i26734c8vb34tr")
        const val GROUP = 167054748
    }
}