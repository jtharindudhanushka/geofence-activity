package com.group5.zonely.core.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class TimeFormatter @Inject constructor() {
    private val absoluteFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun formatRelative(timeMillis: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            timeMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    fun formatAbsolute(timeMillis: Long): String {
        return absoluteFormat.format(Date(timeMillis))
    }
}
