package me.colinmarsch.dawn

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_content_host)

        if (savedInstanceState == null) {
            val fragment = MainFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.main_content, fragment)
                .commit()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val streakItem = menu.findItem(R.id.streaks_menu_option)
        val actionView = streakItem.actionView
        val badgeTextView = actionView.findViewById<TextView>(R.id.streak_badge)
        val sharedPrefs = getSharedPreferences(
            getString(R.string.shared_prefs_name),
            Context.MODE_PRIVATE
        )
        val currentStreak = sharedPrefs.getInt(getString(R.string.saved_streak_key), 0)
        badgeTextView.text = currentStreak.toString()
        actionView.setOnClickListener {
            onOptionsItemSelected(streakItem)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        R.id.streaks_menu_option -> openStreaksScreen()
        else -> super.onOptionsItemSelected(item)
    }

    private fun openStreaksScreen(): Boolean {
        StreaksDialog(this).show()
        return true
    }

    fun transitionToGetUpDelay() {
        val fragment = GetUpDelayFragment()

        supportFragmentManager.beginTransaction()
            .addToBackStack(GetUpDelayFragment.TAG)
            .setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            .replace(R.id.main_content, fragment, GetUpDelayFragment.TAG)
            .commit()
    }

    fun transitionToStayOff() {
        val fragment = StayOffFragment()

        supportFragmentManager.beginTransaction()
            .addToBackStack(StayOffFragment.TAG)
            .setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            .replace(R.id.main_content, fragment, StayOffFragment.TAG)
            .commit()
    }
}
