package me.colinmarsch.dawn

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class InAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.in_app_activity)
        // TODO(colinmarsch) detect when the user leaves this app/activity
        //  display a notification saying they broke their streak

        // TODO(colinmarsch) cancel the alarm that gives the user time on their phone
        // TODO(colinmarsch) cancel the 30 second notification
    }
}