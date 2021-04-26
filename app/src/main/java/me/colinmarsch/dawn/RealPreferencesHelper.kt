package me.colinmarsch.dawn

import android.content.Context
import android.media.RingtoneManager
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.*
import kotlin.collections.HashSet

class RealPreferencesHelper(val context: Context) : PreferencesHelper {

    private val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE)

    override fun getSavedHour(): Int {
        val currentTime = Calendar.getInstance()
        return sharedPrefs.getInt(SAVED_HOUR_KEY, currentTime.get(Calendar.HOUR_OF_DAY))
    }

    override fun setSavedHour(hour: Int) {
        with(sharedPrefs.edit()) {
            putInt(SAVED_HOUR_KEY, hour)
            apply()
        }
    }

    override fun getSavedMinute(): Int {
        val currentTime = Calendar.getInstance()
        return sharedPrefs.getInt(SAVED_MINUTE_KEY, currentTime.get(Calendar.MINUTE))
    }

    override fun setSavedMinute(minute: Int) {
        with(sharedPrefs.edit()) {
            putInt(SAVED_MINUTE_KEY, minute)
            apply()
        }
    }

    override fun getStreak(): Int = sharedPrefs.getInt(STREAK_KEY, 0)

    override fun setStreak(streak: Int) {
        with(sharedPrefs.edit()) {
            putInt(STREAK_KEY, streak)
            apply()
        }
    }

    override fun getVolume(): Int = sharedPrefs.getInt(SAVED_VOLUME_KEY, 0)

    override fun setVolume(volume: Int) {
        with(sharedPrefs.edit()) {
            putInt(SAVED_VOLUME_KEY, volume)
            apply()
        }
    }

    override fun getStayOffTime(): Long = sharedPrefs.getLong(STAY_OFF_KEY, 300000L)

    override fun setStayOffTime(time: Long) {
        with(sharedPrefs.edit()) {
            putLong(STAY_OFF_KEY, time)
            apply()
        }
    }

    override fun getGetUpDelayTime(): Long = sharedPrefs.getLong(GET_UP_DELAY_KEY, 300000L)

    override fun setGetUpDelayTime(time: Long) {
        with(sharedPrefs.edit()) {
            putLong(GET_UP_DELAY_KEY, time)
            apply()
        }
    }

    override fun getRingtoneTitle(): String = sharedPrefs.getString(RINGTONE_TITLE_KEY, "Default")!!

    override fun setRingtoneTitle(title: String) {
        with(sharedPrefs.edit()) {
            putString(RINGTONE_TITLE_KEY, title)
            apply()
        }
    }

    override fun getRingtonePath(): String? {
        val defaultUri =
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE)
        return sharedPrefs.getString(RINGTONE_KEY, defaultUri.path)
    }

    override fun setRingtonePath(path: String) {
        with(sharedPrefs.edit()) {
            putString(RINGTONE_KEY, path)
            apply()
        }
    }

    override fun getFailedDays(): Set<String> =
        sharedPrefs.getStringSet(FAILED_DAYS_KEY, HashSet<String>())!!

    override fun recordFailedDay() {
        val c: Date = Calendar.getInstance().time
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val currentDay: String = df.format(c)
        val newFailedDaysSet =
            HashSet(sharedPrefs.getStringSet(FAILED_DAYS_KEY, HashSet<String>())!!)
        newFailedDaysSet.add(currentDay)
        with(sharedPrefs.edit()) {
            putStringSet(FAILED_DAYS_KEY, newFailedDaysSet)
            apply()
        }
    }

    override fun getSuccessfulDays(): Set<String> =
        sharedPrefs.getStringSet(SUCCESSFUL_DAYS_KEY, HashSet<String>())!!

    override fun recordSuccessfulDay() {
        val c: Date = Calendar.getInstance().time
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val currentDay: String = df.format(c)
        val newSuccessDaysSet =
            HashSet(sharedPrefs.getStringSet(SUCCESSFUL_DAYS_KEY, HashSet<String>())!!)
        newSuccessDaysSet.add(currentDay)
        with(sharedPrefs.edit()) {
            putStringSet(SUCCESSFUL_DAYS_KEY, newSuccessDaysSet)
            apply()
        }
    }

    override fun getSnoozeDuration(): Long {
        val durationString = sharedPrefs.getString(SNOOZE_DURATION_KEY, "10")!!
        return durationString.toInt() * 60000L
    }

    override fun getDarkModeSetting(): String = sharedPrefs.getString(DARK_MODE_KEY, "DEFAULT")!!

    override fun getFirstDayOfWeek(): DayOfWeek =
        when (sharedPrefs.getString(FIRST_DAY_OF_WEEK_KEY, "SUNDAY")) {
            "SUNDAY" -> DayOfWeek.SUNDAY
            "MONDAY" -> DayOfWeek.MONDAY
            "SATURDAY" -> DayOfWeek.SATURDAY
            else -> DayOfWeek.SUNDAY
        }

    override fun getPmConfirmationSetting(): Boolean = sharedPrefs.getBoolean(PM_CONFIRM_KEY, true)

    companion object {
        const val FAILED_DAYS_KEY = "me.colinmarsch.dawn.FAILED_DAYS_KEY"
        const val GET_UP_DELAY_KEY = "me.colinmarsch.dawn.GET_UP_DELAY_KEY"
        const val SAVED_HOUR_KEY = "me.colinmarsch.dawn.SAVED_HOUR_KEY"
        const val SAVED_MINUTE_KEY = "me.colinmarsch.dawn.SAVED_MINUTE_KEY"
        const val RINGTONE_KEY = "me.colinmarsch.dawn.RINGTONE_KEY"
        const val RINGTONE_TITLE_KEY = "me.colinmarsch.dawn.SAVED_RINGTONE_KEY"
        const val STREAK_KEY = "me.colinmarsch.dawn.STREAK_KEY"
        const val SAVED_VOLUME_KEY = "me.colinmarsch.dawn.SAVED_VOLUME_KEY"
        const val SHARED_PREFS_FILE = "me.colinmarsch.dawn.shared_prefs_file"
        const val STAY_OFF_KEY = "me.colinmarsch.dawn.STAY_OFF_KEY"
        const val SUCCESSFUL_DAYS_KEY = "me.colinmarsch.dawn.SUCCESSFUL_DAYS_KEY"
        const val SNOOZE_DURATION_KEY = "me.colinmarsch.dawn.SNOOZE_DURATION_KEY"
        const val DARK_MODE_KEY = "me.colinmarsch.dawn.DARK_MODE_KEY"
        const val FIRST_DAY_OF_WEEK_KEY = "me.colinmarsch.dawn.FIRST_DAY_OF_WEEK_KEY"
        const val PM_CONFIRM_KEY = "me.colinmarsch.dawn.PM_CONFIRM_KEY"
    }
}