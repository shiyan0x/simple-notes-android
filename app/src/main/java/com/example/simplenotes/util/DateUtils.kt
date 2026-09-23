package com.example.simplenotes.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateTimeFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    fun formatFormattedDate(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }
}
