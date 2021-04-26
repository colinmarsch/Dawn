package me.colinmarsch.dawn

import java.time.DayOfWeek

interface PreferencesHelper {
    fun getSavedHour(): Int

    fun setSavedHour(hour: Int)

    fun getSavedMinute(): Int

    fun setSavedMinute(minute: Int)

    fun getStreak(): Int

    fun setStreak(streak: Int)

    fun getVolume(): Int

    fun setVolume(volume: Int)

    fun getStayOffTime(): Long

    fun setStayOffTime(time: Long)

    fun getGetUpDelayTime(): Long

    fun setGetUpDelayTime(time: Long)

    fun getRingtoneTitle(): String

    fun setRingtoneTitle(title: String)

    fun getRingtonePath(): String?

    fun setRingtonePath(path: String)

    fun getFailedDays(): Set<String>

    fun recordFailedDay()

    fun getSuccessfulDays(): Set<String>

    fun recordSuccessfulDay()

    fun getSnoozeDuration(): Long

    fun getDarkModeSetting(): String

    fun getFirstDayOfWeek(): DayOfWeek

    fun getPmConfirmationSetting(): Boolean
}