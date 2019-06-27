package fr.twentynine.keepon.intro

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage
import fr.twentynine.keepon.MainActivity
import fr.twentynine.keepon.R
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
        sliderPageHome.bgColor = Color.parseColor(COLOR_SLIDE_HOME)

        val sliderPageFour = SliderPage()
        sliderPageFour.title = getString(R.string.intro_four_title)
        sliderPageFour.description = getString(R.string.intro_four_desc)
        sliderPageFour.imageDrawable = R.mipmap.img_intro_four
        sliderPageFour.bgColor = Color.parseColor(COLOR_SLIDE_FOUR)

        val sliderPageFive = SliderPage()
        sliderPageFive.title = getString(R.string.intro_five_title)
        sliderPageFive.description = getString(R.string.intro_five_desc)
        sliderPageFive.imageDrawable = R.mipmap.img_intro_five
        sliderPageFive.bgColor = Color.parseColor(COLOR_SLIDE_FIVE)

        val sliderPageSix = SliderPage()
        sliderPageSix.title = getString(R.string.intro_six_title)
        sliderPageSix.description = getString(R.string.intro_six_desc)
        sliderPageSix.imageDrawable = R.mipmap.img_intro_six
        sliderPageSix.bgColor = Color.parseColor(COLOR_SLIDE_SIX)

        addSlide(AppIntroFragment.newInstance(sliderPageHome))
        addSlide(IntroFragmentPermission())
        addSlide(IntroFragmentNotification())
        addSlide(AppIntroFragment.newInstance(sliderPageFour))
        addSlide(AppIntroFragment.newInstance(sliderPageFive))
        addSlide(AppIntroFragment.newInstance(sliderPageSix))

        setFadeAnimation()
        showSkipButton(false)
        isProgressButtonEnabled = true

        KeepOnUtils.startScreenTimeoutObserverService(this)
    }

    override fun onDonePressed(currentFragment: Fragment) {
        if (Settings.System.canWrite(this)) {
            super.onDonePressed(currentFragment)

            KeepOnUtils.setSkipIntro(true, this)
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)

        if (newFragment != null) {
            val title: TextView = newFragment.view!!.findViewById(R.id.title)

            when (title.text) {
                getString(R.string.intro_home_title) -> setNavBarColor(COLOR_SLIDE_HOME)
                getString(R.string.dialog_permission_title) -> setNavBarColor(COLOR_SLIDE_PERM)
                getString(R.string.dialog_notification_title) -> setNavBarColor(COLOR_SLIDE_NOTIF)
                getString(R.string.intro_four_title) -> setNavBarColor(COLOR_SLIDE_FOUR)
                getString(R.string.intro_five_title) -> setNavBarColor(COLOR_SLIDE_FIVE)
                getString(R.string.intro_six_title) -> setNavBarColor(COLOR_SLIDE_SIX)
            }
        }
    }

    override fun onDestroy() {
        KeepOnUtils.stopScreenTimeoutObserverService(this)
        super.onDestroy()
    }

    companion object {
        const val COLOR_SLIDE_PERM = "#ffd800"
        const val COLOR_SLIDE_NOTIF = "#00c3ff"
        private const val COLOR_SLIDE_SIX = "#4caf50"
        private const val COLOR_SLIDE_FIVE = "#bb8930"
        private const val COLOR_SLIDE_FOUR = "#00bcd4"
        private const val COLOR_SLIDE_HOME = "#222222"

        fun newIntent(context: Context): Intent {
            return Intent(context, IntroActivity::class.java)
        }
    }
}