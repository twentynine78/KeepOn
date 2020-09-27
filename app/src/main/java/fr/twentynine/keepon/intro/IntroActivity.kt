package fr.twentynine.keepon.intro

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
import fr.twentynine.keepon.utils.KeepOnUtils


class IntroActivity : AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sliderPageHome = SliderPage()
        sliderPageHome.title = getString(R.string.intro_home_title)
        sliderPageHome.description = getString(R.string.intro_home_desc)
        sliderPageHome.imageDrawable = R.mipmap.img_intro_home
        sliderPageHome.backgroundColor = COLOR_SLIDE_HOME

        val sliderPageFour = SliderPage()
        sliderPageFour.title = getString(R.string.intro_four_title)
        sliderPageFour.description = getString(R.string.intro_four_desc)
        sliderPageFour.imageDrawable = R.mipmap.img_intro_four
        sliderPageFour.backgroundColor = COLOR_SLIDE_FOUR

        val sliderPageFive = SliderPage()
        sliderPageFive.title = getString(R.string.intro_five_title)
        sliderPageFive.description = getString(R.string.intro_five_desc)
        sliderPageFive.imageDrawable = R.mipmap.img_intro_five
        sliderPageFive.backgroundColor = COLOR_SLIDE_FIVE

        addSlide(AppIntroFragment.newInstance(sliderPageHome))
        addSlide(IntroFragmentPermission())
        addSlide(IntroFragmentNotification())
        addSlide(AppIntroFragment.newInstance(sliderPageFour))
        addSlide(AppIntroFragment.newInstance(sliderPageFive))
        addSlide(IntroFragmentAddQSTile())

        setTransformer(AppIntroPageTransformerType.Parallax(
            titleParallaxFactor = 1.0,
            imageParallaxFactor = -1.0,
            descriptionParallaxFactor = 2.0
        ))
        isSkipButtonEnabled = false
        isButtonsEnabled = false
        showStatusBar(true)
        isColorTransitionsEnabled = true

        KeepOnUtils.startScreenTimeoutObserverService(this)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        if (Settings.System.canWrite(this)) {
            super.onDonePressed(currentFragment)

            KeepOnUtils.setSkipIntro(true, this)
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
                getString(R.string.intro_four_title) -> setNavBarColor(COLOR_SLIDE_FOUR)
                getString(R.string.intro_five_title) -> setNavBarColor(COLOR_SLIDE_FIVE)
                getString(R.string.intro_qstile_title) -> setNavBarColor(COLOR_SLIDE_QSTILE)
            }
        }
    }

    companion object {
        val COLOR_SLIDE_PERM = Color.parseColor("#ffd800")
        val COLOR_SLIDE_NOTIF = Color.parseColor("#00c3ff")
        val COLOR_SLIDE_QSTILE = Color.parseColor("#4caf50")
        private val COLOR_SLIDE_FIVE = Color.parseColor("#bb8930")
        private val COLOR_SLIDE_FOUR = Color.parseColor("#00bcd4")
        private val COLOR_SLIDE_HOME = Color.parseColor("#222222")

        fun newIntent(context: Context): Intent {
            return Intent(context.applicationContext, IntroActivity::class.java)
        }
    }
}