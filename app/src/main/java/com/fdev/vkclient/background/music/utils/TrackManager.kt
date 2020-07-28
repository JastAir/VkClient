package com.fdev.vkclient.background.music.utils

import android.content.Context
import com.fdev.vkclient.background.DownloadFileService
import com.fdev.vkclient.background.music.models.Track
import com.fdev.vkclient.model.attachments.Audio
import java.io.File

/**
 * helps store [Audio] in cache using [Track] as an actual model
 */
class TrackManager(private val context: Context) {

    private val dir = File(context.cacheDir, TRACK_DIR)

    init {
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    fun getFile(audio: Audio) = File(dir, audio.fullId + ".mp3")

    fun getExistingPath(audio: Audio): String? {
        val file = getFile(audio)
        return if (file.exists()) {
            file.absolutePath
        } else {
            null
        }
    }

    fun downloadTrack(track: Track, callback: (Track) -> Unit) {
        if (track.isCached()) return
        val url = track.audio.url ?: return

        val trackPath = getFile(track.audio).absolutePath
        DownloadFileService.startService(context, url, trackPath) {
            if (it == trackPath) {
                callback(Track(track.audio, trackPath))
            }
        }
    }

    fun getTrack(audio: Audio) = Track(audio, getExistingPath(audio))

    fun getTracks(audios: List<Audio>) = ArrayList(audios.map { getTrack(it) })

    fun removeTrack(track: Track) {
        getFile(track.audio).delete()
    }

    companion object {
        const val TRACK_DIR = "tracks"
    }

}