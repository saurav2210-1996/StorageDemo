package com.rvt.appusageanalysis

import java.text.SimpleDateFormat
import java.util.*

fun getDateTimeFromTimeStamp(timestamp : Long) : String{
    val formatter  = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(timestamp))
}
