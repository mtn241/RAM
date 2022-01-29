package com.extendes.ram.view.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.extendes.ram.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_activity)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment()).commit()
    }
}