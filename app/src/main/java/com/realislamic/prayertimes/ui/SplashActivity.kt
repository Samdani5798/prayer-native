package com.realislamic.prayertimes.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.realislamic.prayertimes.R
import com.realislamic.prayertimes.data.local.PreferencesManager

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = PreferencesManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val destination = if (prefs.hasCompletedOnboarding) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            startActivity(destination)
            finish()
        }, 900)
    }
}
