package fr.twentynine.keepon.ui.intro

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.github.appintro.indicator.PageIndicatorAnimationType
import com.github.appintro.indicator.PageIndicatorViewIndicatorController
import com.github.appintro.model.SliderPage
import fr.twentynine.keepon.ui.MainActivity
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.fragments.IntroFragmentAddQSTile
import fr.twentynine.keepon.ui.intro.fragments.IntroFragmentNotification
import fr.twentynine.keepon.ui.intro.fragments.IntroFragmentPermission
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy

class IntroActivity : AppIntro2() {

    private val activityUtils: ActivityUtils by lazy()
    private val commonUtils: CommonUtils by lazy()
    private val preferences: Preferences by lazy()

    private var showSlidePerm = true
    private var showSlideNotif = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)

        // Ignore implicit intents, because they are not valid.
        if (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component) {
            finish()
            return
        }

        // Create standard slide
        val sliderPageHome = SliderPage()
        sliderPageHome.title = getString(R.string.intro_home_title)
        sliderPageHome.description = getString(R.string.intro_home_desc)
        sliderPageHome.imageDrawable = R.mipmap.img_intro_home
        sliderPageHome.image2Drawable = R.mipmap.img_intro_home_2
        sliderPageHome.backgroundColor = COLOR_SLIDE_HOME

        val sliderPageInfo1 = SliderPage()
        sliderPageInfo1.title = getString(R.string.intro_info1_title)
        sliderPageInfo1.description = getString(R.string.intro_info1_desc)
        sliderPageInfo1.imageDrawable = R.mipmap.img_intro_info1
        sliderPageInfo1.image2Drawable = R.mipmap.img_intro_info1_2
        sliderPageInfo1.backgroundColor = COLOR_SLIDE_INFO1

        val sliderPageInfo2 = SliderPage()
        sliderPageInfo2.title = getString(R.string.intro_info2_title)
        sliderPageInfo2.description = getString(R.string.intro_info2_desc)
        sliderPageInfo2.imageDrawable = R.mipmap.img_intro_info1
        sliderPageInfo2.image2Drawable = R.mipmap.img_intro_info2_2
        sliderPageInfo2.backgroundColor = COLOR_SLIDE_INFO2

        val sliderPageInfo3 = SliderPage()
        sliderPageInfo3.title = getString(R.string.intro_info3_title)
        sliderPageInfo3.description = getString(R.string.intro_info3_desc)
        sliderPageInfo3.imageDrawable = R.mipmap.img_intro_info3
        sliderPageInfo3.image2Drawable = R.mipmap.img_intro_info3_2
        sliderPageInfo3.backgroundColor = COLOR_SLIDE_INFO3

        val sliderPageInfo4 = SliderPage()
        sliderPageInfo4.title = getString(R.string.intro_info4_title)
        sliderPageInfo4.description = getString(R.string.intro_info4_desc)
        sliderPageInfo4.imageDrawable = R.mipmap.img_intro_info4
        sliderPageInfo4.image2Drawable = R.mipmap.img_intro_info4_2
        sliderPageInfo4.backgroundColor = COLOR_SLIDE_INFO4

        // Define slides to show
        showSlidePerm = !Settings.System.canWrite(this.applicationContext)
        showSlideNotif = activityUtils.isNotificationEnabled()
        savedInstanceState?.let {
            showSlidePerm = it.getBoolean(SLIDE_PERM_SHOWED, showSlidePerm)
            showSlideNotif = it.getBoolean(SLIDE_NOTIF_SHOWED, showSlideNotif)
        }

        // Check if it's first launch or help launch
        if (preferences.getSkipIntro()) {
            addSlide(AppIntroFragment.newInstance(sliderPageHome))
            if (showSlidePerm) {
                addSlide(IntroFragmentPermission.newInstance())
            }
            if (showSlideNotif) {
                addSlide(IntroFragmentNotification.newInstance())
            }
            addSlide(AppIntroFragment.newInstance(sliderPageInfo1))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo2))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo3))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo4))
            addSlide(IntroFragmentAddQSTile.newInstance())
        } else {
            addSlide(AppIntroFragment.newInstance(sliderPageHome))
            addSlide(IntroFragmentPermission.newInstance())
            addSlide(IntroFragmentNotification.newInstance())
            addSlide(AppIntroFragment.newInstance(sliderPageInfo1))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo2))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo3))
            addSlide(AppIntroFragment.newInstance(sliderPageInfo4))
            addSlide(IntroFragmentAddQSTile.newInstance())
        }

        setTransformer(
            AppIntroPageTransformerType.Parallax(
                titleParallaxFactor = -1.0,
                imageParallaxFactor = -1.6,
                image2ParallaxFactor = -0.8,
                descriptionParallaxFactor = -1.7
            )
        )

        indicatorController = PageIndicatorViewIndicatorController(this, null, PageIndicatorAnimationType.DROP)

        isWizardMode = !preferences.getSkipIntro()
        isButtonsEnabled = true
        showStatusBar(true)
        isColorTransitionsEnabled = true

        // Set initial timeout for first launch
        if (!preferences.getSkipIntro()) {
            // Start service to monitor screen timeout
            commonUtils.startScreenTimeoutObserverService()

            preferences.setOriginalTimeout(preferences.getCurrentTimeout())
            preferences.setPreviousValue(preferences.getCurrentTimeout())
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        if (Settings.System.canWrite(this)) {
            super.onDonePressed(currentFragment)

            if (!isWizardMode) {
                preferences.setSkipIntro(true)
                startActivity(MainActivity.newIntent(this))
            }
            finish()
        }
    }

    public override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)

        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(SLIDE_PERM_SHOWED, showSlidePerm)
        outState.putBoolean(SLIDE_NOTIF_SHOWED, showSlideNotif)
    }

    companion object {
        internal const val SLIDE_PERM_SHOWED = "SLIDE_PERM_SHOWED"
        internal const val SLIDE_NOTIF_SHOWED = "SLIDE_NOTIF_SHOWED"

        val COLOR_SLIDE_PERM = Color.parseColor("#ffd800")
        val COLOR_SLIDE_NOTIF = Color.parseColor("#00c3ff")
        val COLOR_SLIDE_QSTILE = Color.parseColor("#4caf50")
        private val COLOR_SLIDE_INFO1 = Color.parseColor("#00bcd4")
        private val COLOR_SLIDE_INFO2 = Color.parseColor("#cc3914")
        private val COLOR_SLIDE_INFO3 = Color.parseColor("#6f3aa1")
        private val COLOR_SLIDE_INFO4 = Color.parseColor("#d68f0b")
        private val COLOR_SLIDE_HOME = Color.parseColor("#222222")

        fun newIntent(context: Context): Intent {
            return Intent(context, IntroActivity::class.java)
        }
    }
}
