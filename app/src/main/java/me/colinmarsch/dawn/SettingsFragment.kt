package me.colinmarsch.dawn

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val preferenceManager: PreferenceManager = preferenceManager
        preferenceManager.sharedPreferencesName = SHARED_PREFS_FILE
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = getString(R.string.settings)
    }

    companion object {
        const val TAG = "SETTINGS_FRAGMENT_TAG"
        const val SHARED_PREFS_FILE = "me.colinmarsch.dawn.shared_prefs_file"
    }
}