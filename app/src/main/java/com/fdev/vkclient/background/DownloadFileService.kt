package com.fdev.vkclient.background

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fdev.vkclient.App
import com.fdev.vkclient.R
import com.fdev.vkclient.lg.Lg
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.writeResponseBodyToDisk
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

class DownloadFileService : IntentService(NAME) {

    @Inject
    lateinit var api: ApiService

    private var disposable: Disposable? = null

    private lateinit var url: String
    private lateinit var file: File

    companion object {

        const val NAME = "DownloadFileService"
        const val KEY_FILE_URL = "fileUrl"
        const val KEY_FILE_DEST = "fileDest"
        const val KEY_OVERRIDE = "override"

        private const val CHANNEL_ID = NAME

        /**
         * notifies about the end of downloading
         * Pair<String, Boolean> is for file's path and success status
         */
        private val bus = PublishSubject.create<Pair<String, Boolean>>()

        /**
         * [url] what to download
         * [file] where to download to
         * [override] should override file if exists
         * [onFileDownloaded] callback after the task is done: null if error
         */
        fun startService(
                context: Context?,
                url: String,
                path: String,
                override: Boolean = false,
                onFileDownloaded: (String?) -> Unit = {}
        ): Disposable {
            val intent = Intent(context, DownloadFileService::class.java).apply {
                putExtra(KEY_FILE_URL, url)
                putExtra(KEY_FILE_DEST, path)
                putExtra(KEY_OVERRIDE, override)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(intent)
            } else {
                context?.startService(intent)
            }
            return bus.subscribe { pair ->
                if (pair.first == path) {
                    onFileDownloaded(if (pair.second) path else null)
                }
            }
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        l("starting")
        App.appComponent?.inject(this)
        showNotification()
        url = intent?.extras?.getString(KEY_FILE_URL) ?: return
        file = File(intent.extras?.getString(KEY_FILE_DEST) ?: return)
        if (intent.extras?.getBoolean(KEY_OVERRIDE) == false && file.exists()) {
            disposable = Single.just(true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(::deliverResult)
            return
        }
        disposable = api.downloadFile(url)
                .map { writeResponseBodyToDisk(it, file.absolutePath) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::deliverResult) {
                    lw("error during downloading: ${it.message}")
                    it.printStackTrace()
                    deliverResult(false)
                }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    private fun showNotification() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.downloading_file))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        val id = Random(System.currentTimeMillis()).nextInt(0, 65535)
        startForeground(id, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun deliverResult(success: Boolean) {
        l("success = $success for ${file.absolutePath}")
        bus.onNext(Pair(file.absolutePath, success))
    }

    private fun l(s: String) {
        Lg.i("[download] $s")
    }

    private fun lw(s: String) {
        Lg.wtf("[download] $s")
    }
}