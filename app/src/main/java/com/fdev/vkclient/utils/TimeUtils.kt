package com.fdev.vkclient.utils

import android.annotation.SuppressLint
import com.fdev.vkclient.lg.Lg
import java.text.SimpleDateFormat
import java.util.*


const val SS = ":ss"
const val HH_MM = "HH:mm"
const val DD_MM = "dd.MM"
const val DD_MMM = "dd MMM"
const val DD_MM_YYYY = "dd.MM.yyyy"
const val DD_MMM_YYYY = "dd MMM yyyy"

@SuppressLint("SimpleDateFormat")
fun getTime(ts: Int, shortened: Boolean = false, withSeconds: Boolean = false, noDate: Boolean = false, format: String? = null): String {
    val date = Date(ts * 1000L)
    val today = Date()
    if (format != null) {
        return SimpleDateFormat(format).format(date)
    }
    val seconds = if (withSeconds) SS else ""
    val fmt = when {
        noDate ||
                today.day == date.day &&
                today.month == date.month &&
                today.year == date.year -> "$HH_MM$seconds"
        !shortened && today.year == date.year -> "$HH_MM$seconds $DD_MMM"
        !shortened -> "$HH_MM$seconds $DD_MMM_YYYY"
        today.year == date.year -> DD_MMM
        else -> DD_MMM_YYYY
    }
    return SimpleDateFormat(fmt).format(date).toLowerCase()
}

@SuppressLint("SimpleDateFormat")
fun getDate(ts: Int): String {
    val date = Date(ts * 1000L)
    val today = Date()
    val fmt = when {
        today.year == date.year -> DD_MMM
        else -> DD_MMM_YYYY
    }
    return SimpleDateFormat(fmt).format(date).toLowerCase()
}

fun secToTime(sec: Int): String {
    val min = sec / 60
    val secn = sec % 60
    return if (secn > 9) {
        "$min:$secn"
    } else {
        "$min:0$secn"
    }
}

@SuppressLint("SimpleDateFormat")
fun formatDate(date: String): String = try {
    val isTruncated = date.count { it == '.' } == 1
    val inPattern = if (isTruncated) DD_MM else DD_MM_YYYY
    val outPattern = if (isTruncated) DD_MMM else DD_MMM_YYYY
    SimpleDateFormat(outPattern).format(SimpleDateFormat(inPattern).parse(date))
} catch (e: Exception) {
    Lg.wtf("format date: ${e.message}")
    e.printStackTrace()
    date
}

/**
 * fucking vk format allows "d.M.yyyy" WTF???
 */
fun formatBdate(bdate: String?) = bdate
        ?.split(".")
        ?.map {
            if (it.length == 1) "0$it" else it
        }
        ?.joinToString(".") ?: ""

fun time() = (System.currentTimeMillis() / 1000L).toInt()