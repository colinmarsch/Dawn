package me.colinmarsch.dawn

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferenceManager: PreferenceManager = preferenceManager
        preferenceManager.sharedPreferencesName = RealPreferencesHelper.SHARED_PREFS_FILE
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = getString(R.string.settings)
    }

    override fun onResume() {
        super.onResume()

        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPrefs: SharedPreferences, key: String?) {
        val prefsHelper = RealPreferencesHelper(requireContext())
        when (key) {
            RealPreferencesHelper.DARK_MODE_KEY -> {
                when (prefsHelper.getDarkModeSetting()) {
                    "DEFAULT" -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                    "ON" -> setDefaultNightMode(MODE_NIGHT_YES)
                    "OFF" -> setDefaultNightMode(MODE_NIGHT_NO)
                }
            }
        }
    }

    companion object {
        const val TAG = "SETTINGS_FRAGMENT_TAG"
    }
}