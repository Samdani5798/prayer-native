package com.realislamic.prayertimes.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.realislamic.prayertimes.data.local.PreferencesManager
import com.realislamic.prayertimes.util.LocaleHelper

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferencesManager(newBase)
        val wrapped = LocaleHelper.applyLocale(newBase, prefs.languageCode)
        super.attachBaseContext(wrapped)
    }
}
