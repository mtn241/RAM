package com.extendes.ram.view.ui


import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.extendes.ram.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}