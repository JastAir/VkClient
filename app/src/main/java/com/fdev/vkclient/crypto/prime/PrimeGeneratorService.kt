package com.fdev.vkclient.crypto.prime

import android.app.Service
import android.content.Context
import android.content.Intent

/**
 * Created by fuckyou on 12.12.2017.
 * generates safe prime for DH-2048
 */

class PrimeGeneratorService : Service() {

    private val core by lazy { PrimeGeneratorCore(applicationContext) }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        core.run()
        return START_STICKY
    }

    companion object {
        fun launch(context: Context) {
            context.startService(Intent(context, PrimeGeneratorService::class.java))
        }
    }
}