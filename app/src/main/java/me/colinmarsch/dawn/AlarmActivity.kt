package me.colinmarsch.dawn

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import me.colinmarsch.dawn.NotificationHelper.Companion.NOTIF_ID
import me.colinmarsch.dawn.NotificationHelper.Companion.TIME_NOTIF_ID

class AlarmActivity: AppCompatActivity() {
    private lateinit var stopAlarmButton: Button
    private val mediaHandler = MediaHandler()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_activity)
        stopAlarmButton = findViewById(R.id.stop_alarm_button)
        stopAlarmButton.setOnClickListener {
            mediaHandler.stopAlarm()
            with(NotificationManagerCompat.from(this)) {
                cancel(NOTIF_ID)
            }
            with(NotificationManagerCompat.from(this)) {
                cancel(TIME_NOTIF_ID)
            }
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        mediaHandler.startAlarm(this)
    }
}
