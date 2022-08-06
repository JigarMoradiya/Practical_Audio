package com.jigar.audioplayer.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    const val DDMMM: String = "dd MMM"
    const val yyyyMMdd_HH_mm_ss: String = "yyyyMMdd_HH_mm_ss"
    fun getCurrentTimeStamp() : String {
        val dateFormat = SimpleDateFormat(yyyyMMdd_HH_mm_ss)
        return dateFormat.format(Date())
    }
    fun secToTimeFormat(secs: Long?): String {
        val separator = "   •   "
        if (secs==null || secs == 0L){
            return separator+"00:00"
        }
        val totalSecs = secs
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        return if (hours > 0){
            separator + String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }else if (minutes > 0){
            separator + String.format("%02d:%02d", minutes, seconds)
        }else{
            "$separator$seconds s"
        }
    }

    fun secToTime(secs: Long?): String {
        if (secs==null || secs == 0L){
            return "00:00"
        }
        val totalSecs = secs
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        return if (hours > 0){
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }else{
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun getDateString(date: Date?): String {
        val dateString: String
        val newDate = date ?: Date()
        val nowDate = Date()

        val separator = ""
        when {
            isSameDay(nowDate, newDate) -> {
                val cal = Calendar.getInstance()
                cal.time = newDate
                val hourDiff = (Date().time - newDate.time) / 1000 / 3600
                val minDiff = ((nowDate.time - newDate.time) / 1000 % 3600) / 60
                dateString = if (minDiff < 1) {
                    "$separator just now"
                } else if (hourDiff < 1) {
                    "$separator $minDiff min ago"
                } else {
                    "$separator $hourDiff hour ago"
                }
            }
            isDateYesterday(newDate) -> dateString = "$separator yesterday"
            else -> dateString = "$separator${DateFormat.format(DDMMM, newDate)}"
        }
        return dateString
    }

    private fun isDateYesterday(time: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)

        cal1.time = time
        cal2.time = calendar.time
        return (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(
            Calendar.DAY_OF_MONTH
        )
                == cal2.get(Calendar.DAY_OF_MONTH) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
    }

    private fun isSameDay(nowDate: Date, newDate: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()

        cal1.time = nowDate
        cal2.time = newDate
        return (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.DAY_OF_MONTH)
                == cal2.get(Calendar.DAY_OF_MONTH) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
    }
}