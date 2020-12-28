package me.colinmarsch.dawn

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
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

    fun transitionToGetUpDelay(sharedElement: View, destElementName: String) {
        val fragment = GetUpDelayFragment()

        supportFragmentManager.beginTransaction()
            .addToBackStack(GetUpDelayFragment.TAG)
            .addSharedElement(sharedElement, destElementName)
            .replace(R.id.main_content, fragment, GetUpDelayFragment.TAG)
            .commit()
    }

    fun transitionToStayOff(sharedElement: View, destElementName: String) {
        val fragment = StayOffFragment()

        supportFragmentManager.beginTransaction()
            .addToBackStack(StayOffFragment.TAG)
            .addSharedElement(sharedElement, destElementName)
            .replace(R.id.main_content, fragment, StayOffFragment.TAG)
            .commit()
    }
}
