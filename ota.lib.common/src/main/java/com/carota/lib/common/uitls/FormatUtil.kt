package com.carota.lib.common.uitls

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormatUtil {

    companion object{
        fun formatHour(timestamp: Long): String {
            return formatTime(timestamp, "HH:mm")
        }

        private fun formatTime(timestamp: Long, form: String?): String {
            val date = Date(timestamp)
            val format: SimpleDateFormat = createDefaultDateFormat(form)
            return format.format(date) ?: ""
        }

        private fun createDefaultDateFormat(pattern: String?, locale: Locale = Locale.US): SimpleDateFormat {
            return createDateFormat(pattern, locale)
        }

        private fun createDateFormat(pattern: String?, locale: Locale?): SimpleDateFormat {
            return SimpleDateFormat(pattern, locale)
        }
    }
}