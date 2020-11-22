package fr.twentynine.keepon.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import fr.twentynine.keepon.R
import fr.twentynine.keepon.databinding.ActivitySplashScreenBinding
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import fr.twentynine.keepon.utils.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import toothpick.ktp.delegate.lazy

class SplashScreen : AppCompatActivity() {

    private val bundleScrubber: BundleScrubber by lazy()
    private val commonUtils: CommonUtils by lazy()
    private val preferences: Preferences by lazy()

    private val binding: ActivitySplashScreenBinding by viewBinding(ActivitySplashScreenBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)

        // Manage shortcut
        if (intent.action == CommonUtils.ACTION_SHORTCUT_SET_TIMEOUT) {
            // Prevent Activity to bring to front
            moveTaskToBack(true)

            // A hack to prevent a private serializable classloader attack and ignore implicit intents, because they are not valid
            if (bundleScrubber.scrub(intent) || (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component)) {
                finish()
                return
            }

            // Set new timeout and finish activity
            if (intent.extras != null) {
                var timeout = intent.getIntExtra("timeout", 0)
                if (timeout != 0) {
                    if (timeout == -42) timeout = preferences.getOriginalTimeout()
                    if (timeout == -43) timeout = preferences.getPreviousValue()

                    preferences.setTimeout(timeout)
                }
            }
            finish()
            return
        }

        // Start service to monitor screen timeout
        commonUtils.startScreenTimeoutObserverService()

        // Start service to monitor screen off if needed
        if (preferences.getKeepOnState() && preferences.getResetTimeoutOnScreenOff()) {
            commonUtils.startScreenOffReceiverService()
        }

        // Close this Activity if application already running
        if (!isTaskRoot) {
            finish()
            return
        }

        // Set bounce animation on the logo and title
        val bounceAnimLogo: Animation = AnimationUtils.loadAnimation(this, R.anim.splash_bounce_logo)
        val bounceAnimTitle: Animation = AnimationUtils.loadAnimation(this, R.anim.splash_bounce_title)
        binding.logoIv.startAnimation(bounceAnimLogo)
        binding.titleTv.startAnimation(bounceAnimTitle)

        // Set DarkTheme
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            preferences.setDarkTheme(true)
        }
        if (preferences.getDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        setContentView(binding.root)

        if (!preferences.getSkipIntro()) {
            // Start Intro on first launch
            lifecycleScope.launch(Dispatchers.Default) {
                delay(SPLASH_TIME_OUT)
                startActivity(IntroActivity.newIntent(this@SplashScreen.applicationContext))
                finish()
            }
        } else {
            // Launch MainActivity or show animated loading text
            lifecycleScope.launch(Dispatchers.Default) {
                var animateCount = 100
                delay(SPLASH_TIME_OUT)
                repeat(600) {
                    if (preferences.getAppIsLaunched()) {
                        startActivity(MainActivity.newIntent(this@SplashScreen.applicationContext))
                        finish()
                        return@launch
                    } else {
                        animateCount++
                        if (animateCount >= 10) {
                            launch(Dispatchers.Main) {
                                setAnimatedLoadingText()
                                if (binding.loadingTv.visibility != View.VISIBLE) {
                                    binding.loadingTv.visibility = View.VISIBLE
                                }
                            }
                            animateCount = 0
                        }
                        delay(100)
                    }
                }
                launch(Dispatchers.Main) {
                    binding.loadingTv.visibility = View.INVISIBLE
                    binding.loadingTv.text = getString(R.string.splash_cannot_start_text)
                    binding.loadingTv.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setAnimatedLoadingText() {
        val suffixLength = 3
        var nbDot = binding.loadingTv.text.filter { it == '.' }.count()
        if (nbDot >= suffixLength) {
            nbDot = 1
        } else {
            nbDot++
        }
        binding.loadingTv.text = getString(R.string.splash_loading_text)
            .plus(".".repeat(nbDot))
            .plus(" ".repeat(suffixLength - nbDot))
    }

    companion object {
        const val SPLASH_TIME_OUT: Long = 750

        fun newIntent(context: Context): Intent {
            return Intent(context, SplashScreen::class.java)
        }
    }
}
