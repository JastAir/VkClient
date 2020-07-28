package com.fdev.vkclient.activities

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fdev.vkclient.App
import com.fdev.vkclient.BuildConfig
import com.fdev.vkclient.R
import com.fdev.vkclient.managers.Prefs
import com.fdev.vkclient.model.attachments.Doc
import com.fdev.vkclient.network.ApiService
import com.fdev.vkclient.utils.*
import com.fdev.vkclient.views.TextInputAlertDialog
import kotlinx.android.synthetic.main.activity_exception.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import kotlin.random.Random

class ExceptionActivity : AppCompatActivity() {

    companion object {
        const val ERROR = "error"
    }

    @Inject
    lateinit var api: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        NightModeHelper.updateConfig(
                if (Prefs.isLightTheme) {
                    Configuration.UI_MODE_NIGHT_NO
                } else {
                    Configuration.UI_MODE_NIGHT_YES
                },
                this, R.style.AppTheme
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exception)
        App.appComponent?.inject(this)
        var error = ""
        if (intent.extras != null) {
            error = intent.extras.getString(ERROR)
            tvStack.text = error
        }
        btnReport.setOnClickListener {
            showDialog(error)
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.background)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.background)
    }

    private fun showDialog(error: String) {
        val dialog = TextInputAlertDialog(
                this,
                getString(R.string.describe_actions)
        ) {
            val file = File(cacheDir, "crash_in_${BuildConfig.VERSION_NAME}_${getTime(time())}.txt")
            val writer = BufferedWriter(FileWriter(file))
            writer.write("$it\n$error")
            writer.close()
            sendError(file.absolutePath)
        }

        dialog.show()
    }

    private fun sendError(path: String) {
        rlLoader.show()
        api.getDocUploadServer("doc")
                .subscribeSmart({ uploadServer ->
                    val file = File(path)
                    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    api.uploadDoc(uploadServer.uploadUrl ?: return@subscribeSmart, body)
                            .compose(applySchedulers())
                            .subscribe({ response ->
                                api.saveDoc(response.file ?: return@subscribe)
                                        .subscribeSmart({
                                            val doc = it.getOrNull(0) ?: return@subscribeSmart

                                            api.sendMessage(-App.GROUP, getRandomId(), attachments = doc.getId())
                                                    .subscribeSmart({ response ->
                                                        rlLoader.hide()
                                                        deleteReport(response)
                                                        deleteDoc(doc)
                                                        showToast(this, R.string.report_sent)
                                                        Handler().postDelayed({ onBackPressed() }, 400L)
                                                    }, { error ->
                                                        showError(this, error)
                                                    })

                                        }, { error ->
                                            showError(this, error)
                                        })
                            }, {
                                showError(this, it.message ?: "")
                            })
                }, {
                    showError(this, it)
                })
    }

    private fun getRandomId() = Random.nextInt()

    private fun deleteReport(mid: Int) {
        api.deleteMessages("$mid", 0)
                .subscribeSmart({}, {})
    }

    private fun deleteDoc(doc: Doc) {
        api.deleteDoc(doc.ownerId, doc.id)
                .subscribeSmart({}, {})
    }

    override fun onBackPressed() {
        super.onBackPressed()
        restartApp(this)
    }
}