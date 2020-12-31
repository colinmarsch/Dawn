package me.colinmarsch.dawn

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {
    private lateinit var stopAlarmButton: Button
    private lateinit var snoozeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alarm_activity)

        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        stopAlarmButton = findViewById(R.id.stop_alarm_button)
        stopAlarmButton.setOnClickListener { broadcastPress("STOP") }

        snoozeButton = findViewById(R.id.snooze_button)
        snoozeButton.setOnClickListener { broadcastPress("SNOOZE") }
    }

    private fun broadcastPress(case: String) {
        val stopIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("CASE", case)
        }
        sendBroadcast(stopIntent)
        finish()
    }
}
