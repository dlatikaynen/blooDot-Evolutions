package oy.sarjakuvat.flamingin.bde

import android.app.Activity
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import oy.sarjakuvat.flamingin.bde.input.BtControllerDriver

class SettingsActivity : Activity() {
    private val btDriver: BtControllerDriver = BtControllerDriver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        btDriver.initiateDiscoverAndConnect()
        val error = btDriver.getError()
        val status = btDriver.getStatus()
        error + status
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}
