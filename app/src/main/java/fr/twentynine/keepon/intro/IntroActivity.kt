package fr.twentynine.keepon.intro

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.github.appintro.model.SliderPage
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.fragments.IntroFragmentAddQSTile
import fr.twentynine.keepon.intro.fragments.IntroFragmentNotification
import fr.twentynine.keepon.intro.fragments.IntroFragmentPermission
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.KeepOnUtils
import fr.twentynine.keepon.utils.Preferences

class IntroActivity : AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A hack to prevent a private serializable classloader attack
        if (BundleScrubber.scrub(intent)) {
            finish()
            return
        }

        // Ignore implicit intents, because they are not valid.
        if (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component) {
            finish()
            return
        }

        val sliderPageHome = SliderPage()
        sliderPageHome.title = getString(R.string.intro_home_title)
        sliderPageHome.description = getString(R.string.intro_home_desc)
        sliderPageHome.imageDrawable = R.mipmap.img_intro_home
        sliderPageHome.backgroundColor = COLOR_SLIDE_HOME

        val sliderPageInfo1 = SliderPage()
        sliderPageInfo1.title = getString(R.string.intro_info1_title)
        sliderPageInfo1.description = getString(R.string.intro_info1_desc)
        sliderPageInfo1.imageDrawable = R.mipmap.img_intro_info1
        sliderPageInfo1.backgroundColor = COLOR_SLIDE_INFO1

        val sliderPageInfo2 = SliderPage()
        sliderPageInfo2.title = getString(R.string.intro_info2_title)
        sliderPageInfo2.description = getString(R.string.intro_info2_desc)
        sliderPageInfo2.imageDrawable = R.mipmap.img_intro_info2
        sliderPageInfo2.backgroundColor = COLOR_SLIDE_INFO2

        val sliderPageInfo3 = SliderPage()
        sliderPageInfo3.title = getString(R.string.intro_info3_title)
        sliderPageInfo3.description = getString(R.string.intro_info3_desc)
        sliderPageInfo3.imageDrawable = R.mipmap.img_intro_info3
        sliderPageInfo3.backgroundColor = COLOR_SLIDE_INFO3

        // Check if it's first launch or help launch
        if (Preferences.getSkipIntro(this)) {
            addSlide(AppIntroFragment.newInstance(sliderPageHome))
            if (!Settings.System.canWrite(this.applicationContext)) addSlide(IntroFragmentPermission())
            if (KeepOnUtils.isNotificationEnabled(this)) addSlide(IntroFragmentNotification())
            addSlide(AppIntroFragment.newInstance(sliderPageInfo1))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo2))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo3))
            addSlide(IntroFragmentAddQSTile())
        } else {
            addSlide(AppIntroFragment.newInstance(sliderPageHome))
            addSlide(IntroFragmentPermission())
            addSlide(IntroFragmentNotification())
            addSlide(AppIntroFragment.newInstance(sliderPageInfo1))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo2))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo3))
            addSlide(IntroFragmentAddQSTile())
        }

        setTransformer(
            AppIntroPageTransformerType.Parallax(
                titleParallaxFactor = 1.0,
                imageParallaxFactor = -1.0,
                descriptionParallaxFactor = 2.0
            )
        )
        isSkipButtonEnabled = Preferences.getSkipIntro(this)
        isButtonsEnabled = false
        showStatusBar(true)
        isColorTransitionsEnabled = true

        KeepOnUtils.startScreenTimeoutObserverService(this)

        // Set initial timeout for first launch
        if (!Preferences.getSkipIntro(this)) {
            Preferences.setOriginalTimeout(Preferences.getCurrentTimeout(this), this)
            Preferences.setPreviousValue(Preferences.getCurrentTimeout(this), this)
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        if (Settings.System.canWrite(this)) {
            super.onDonePressed(currentFragment)

            Preferences.setSkipIntro(true, this)
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)

        if (newFragment != null && newFragment.view != null) {
            val title: TextView = newFragment.requireView().findViewById(R.id.title)

            when (title.text) {
                getString(R.string.intro_home_title) -> setNavBarColor(COLOR_SLIDE_HOME)
                getString(R.string.dialog_permission_title) -> setNavBarColor(COLOR_SLIDE_PERM)
                getString(R.string.dialog_notification_title) -> setNavBarColor(COLOR_SLIDE_NOTIF)
                getString(R.string.intro_info1_title) -> setNavBarColor(COLOR_SLIDE_INFO1)
                getString(R.string.intro_info2_title) -> setNavBarColor(COLOR_SLIDE_INFO2)
                getString(R.string.intro_info3_title) -> setNavBarColor(COLOR_SLIDE_INFO3)
                getString(R.string.intro_qstile_title) -> setNavBarColor(COLOR_SLIDE_QSTILE)
            }
        }
    }

    public override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    companion object {
        val COLOR_SLIDE_PERM = Color.parseColor("#ffd800")
        val COLOR_SLIDE_NOTIF = Color.parseColor("#00c3ff")
        val COLOR_SLIDE_QSTILE = Color.parseColor("#4caf50")
        private val COLOR_SLIDE_INFO2 = Color.parseColor("#bb8930")
        private val COLOR_SLIDE_INFO1 = Color.parseColor("#00bcd4")
        private val COLOR_SLIDE_INFO3 = Color.parseColor("#6f3aa1")
        private val COLOR_SLIDE_HOME = Color.parseColor("#222222")

        fun newIntent(context: Context): Intent {
            return Intent(context.applicationContext, IntroActivity::class.java)
        }
    }
}
