package com.fdev.vkclient.activities

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fdev.vkclient.App.Companion.context
import com.fdev.vkclient.R
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.utils.ExceptionHandler
import com.fdev.vkclient.utils.NightModeHelper
import com.fdev.vkclient.utils.stylize
import com.fdev.vkclient.utils.stylizeAll
import com.google.firebase.analytics.FirebaseAnalytics
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

/**
 * all its children will support theme applying
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))

        window.decorView.systemUiVisibility =
                // Tells the system that the window wishes the content to
                // be laid out at the most extreme scenario. See the docs for
                // more information on the specifics
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        // Tells the system that the window wishes the content to
                        // be laid out as if the navigation bar was hidden
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Prefs.isLightTheme) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        updateConfig()
        super.setContentView(layoutResID)
    }

    override fun onResume() {
        updateConfig()
        super.onResume()
        window.statusBarColor = getStatusBarColor()
        window.navigationBarColor = getNavigationBarColor()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item?.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    protected open fun getThemeId() = R.style.AppTheme

    protected open fun getStatusBarColor() = Color.TRANSPARENT

    protected open fun getNavigationBarColor() = ContextCompat.getColor(this, R.color.navigation_bar)

    protected open fun styleScreen(container: ViewGroup) {
        container.stylizeAll()
        stylize()
    }

    private fun updateConfig() {
        NightModeHelper.updateConfig(
                if (Prefs.isLightTheme) {
                    Configuration.UI_MODE_NIGHT_NO
                } else {
                    Configuration.UI_MODE_NIGHT_YES
                },
                this, getThemeId()
        )
    }

    fun firebaseSendEvent(key: String, value: String) {
        val params = Bundle()
        params.putString(key, value)
        FirebaseAnalytics.getInstance(context)
                .logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params)
    }
}