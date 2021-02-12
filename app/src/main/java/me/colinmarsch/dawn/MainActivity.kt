package me.colinmarsch.dawn

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*

class MainActivity : AppCompatActivity() {

    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_content_host)

        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        if (savedInstanceState == null) {
            val fragment = MainFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.main_content, fragment)
                .commit()
        }

        updateUpButton()
        supportFragmentManager.addOnBackStackChangedListener {
            updateUpButton()
        }

        prefsHelper = RealPreferencesHelper(this)
    }

    override fun onResume() {
        super.onResume()

        when (prefsHelper.getDarkModeSetting()) {
            "DEFAULT" -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            "ON" -> setDefaultNightMode(MODE_NIGHT_YES)
            "OFF" -> setDefaultNightMode(MODE_NIGHT_NO)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val settingsItem = menu.findItem(R.id.settings_menu_option)
        settingsItem.isVisible = supportFragmentManager.fragments.first() !is SettingsFragment
        val streakItem = menu.findItem(R.id.streaks_menu_option)
        val actionView = streakItem.actionView
        val badgeTextView = actionView.findViewById<TextView>(R.id.streak_badge)
        val prefsHelper = RealPreferencesHelper(this)
        val currentStreak = prefsHelper.getStreak()
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
        R.id.settings_menu_option -> openSettings()
        else -> super.onOptionsItemSelected(item)
    }

    private fun updateUpButton() {
        invalidateOptionsMenu()
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun openStreaksScreen(): Boolean {
        StreaksDialog(this).show()
        return true
    }

    private fun openSettings(): Boolean {
        val fragment = SettingsFragment()

        supportFragmentManager.beginTransaction()
            .addToBackStack(SettingsFragment.TAG)
            .setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            .replace(R.id.main_content, fragment, SettingsFragment.TAG)
            .commit()

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
