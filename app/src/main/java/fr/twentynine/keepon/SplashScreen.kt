package fr.twentynine.keepon

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import fr.twentynine.keepon.intro.IntroActivity
import fr.twentynine.keepon.receivers.ServicesManagerReceiver
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.preferences.Preferences
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Manage shortcut
        if (intent.action == ServicesManagerReceiver.ACTION_SET_TIMEOUT) {
            // Prevent Activity to bring to front
            moveTaskToBack(true)

            // A hack to prevent a private serializable classloader attack and ignore implicit intents, because they are not valid
            if (BundleScrubber.scrub(intent) ||
                (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component)
            ) {
                finish()
                return
            }

            if (intent.extras != null) {
                val extraKey = "timeout"
                val timeout = intent.getIntExtra(extraKey, 0)
                if (timeout != 0) {
                    val broadcastIntent = Intent(
                        this,
                        ServicesManagerReceiver::class.java
                    )
                    broadcastIntent.action = intent.action
                    broadcastIntent.putExtra(extraKey, timeout)

                    sendBroadcast(broadcastIntent)
                }
            }
            finish()
            return
        }

        // Close this Activity if application already running
        if (!isTaskRoot) {
            finish()
            return
        }

        setContentView(R.layout.activity_splash_screen)

        // Set bounce animation on the logo and title
        val bounceAnimLogo: Animation = AnimationUtils.loadAnimation(
            this,
            R.anim.splash_bounce_logo
        )
        logo_iv.startAnimation(bounceAnimLogo)

        val bounceAnimTitle: Animation = AnimationUtils.loadAnimation(
            this,
            R.anim.splash_bounce_title
        )
        title_tv.startAnimation(bounceAnimTitle)

        // Set DarkTheme
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            Preferences.setDarkTheme(true, this)
        }

        if (Preferences.getDarkTheme(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        if (!Preferences.getSkipIntro(this)) {
            // Start Intro on first launch
            lifecycleScope.launch(Dispatchers.Main) {
                delay(SPLASH_TIME_OUT)
                startActivity(IntroActivity.newIntent(this@SplashScreen))
                finish()
            }
        } else {
            // Launch MainActivity
            lifecycleScope.launch(Dispatchers.Main) {
                delay(SPLASH_TIME_OUT)
                val mainIntent = MainActivity.newIntent(this@SplashScreen)
                startActivity(mainIntent)
                finish()
            }
        }
    }

    companion object {
        const val SPLASH_TIME_OUT: Long = 750

        fun newIntent(context: Context): Intent {
            return Intent(context, SplashScreen::class.java)
        }
    }
}
