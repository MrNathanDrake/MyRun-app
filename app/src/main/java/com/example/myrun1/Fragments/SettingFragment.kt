package com.example.myrun1.Fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.myrun1.Activities.ProfileActivity
import com.example.myrun1.R

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val userProfilePref: Preference? = findPreference("user_profile")
        val webpagePref: Preference? = findPreference("webpage")

        userProfilePref?.setOnPreferenceClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            startActivity(intent)
            true
        }

        webpagePref?.setOnPreferenceClickListener {
            // Open the web link
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sfu.ca/computing.html"))
            startActivity(intent)
            true
        }
    }
}

