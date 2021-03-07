package me.colinmarsch.dawn

import java.util.*

fun Calendar.hourText() =
    if (get(Calendar.HOUR_OF_DAY) == 12 || get(Calendar.HOUR_OF_DAY) == 0) {
        "12"
    } else {
        (get(Calendar.HOUR_OF_DAY) % 12).toString()
    }

fun Calendar.minuteText(): String {
    val minuteNum = get(Calendar.MINUTE)
    return if (minuteNum < 10) {
        "0$minuteNum"
    } else {
        minuteNum.toString()
    }
}