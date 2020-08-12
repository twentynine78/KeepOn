package fr.twentynine.keepon

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import fr.twentynine.keepon.intro.IntroActivity
import fr.twentynine.keepon.utils.KeepOnUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Close this Activity if application already running
        if (!isTaskRoot) {
            finish()
            return
        }

        setContentView(R.layout.activity_splash_screen)

        // Set bounce animation on the logo and title
        val bounceAnimLogo: Animation = AnimationUtils.loadAnimation(this, R.anim.splash_bounce_logo)
        val logo: ImageView = findViewById(R.id.logo)
        logo.startAnimation(bounceAnimLogo)

        val bounceAnimTitle: Animation = AnimationUtils.loadAnimation(this, R.anim.splash_bounce_title)
        val title: TextView = findViewById(R.id.title)
        title.startAnimation(bounceAnimTitle)

        // Set DarkTheme
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)
            KeepOnUtils.setDarkTheme(true, this)

        if (KeepOnUtils.getDarkTheme(this))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        if (!KeepOnUtils.getSkipIntro(this)) {
            //Start Intro on first launch
            CoroutineScope(Dispatchers.Main).launch {
                delay(SPLASH_TIME_OUT)
                startActivity(IntroActivity.newIntent(applicationContext))
                finish()
            }
        } else {
            // Launch MainActivity
            CoroutineScope(Dispatchers.Main).launch {
                delay(SPLASH_TIME_OUT)
                val mainIntent = MainActivity.newIntent(applicationContext)
                startActivity(mainIntent)
                finish()
            }
        }
    }

    companion object {
        const val SPLASH_TIME_OUT: Long = 1000

        fun newIntent(context: Context): Intent {
            return Intent(context.applicationContext, SplashScreen::class.java)
        }
    }
}