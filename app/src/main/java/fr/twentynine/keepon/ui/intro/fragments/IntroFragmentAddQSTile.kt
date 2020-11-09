package fr.twentynine.keepon.ui.intro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity.Companion.COLOR_SLIDE_QSTILE
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import kotlinx.android.synthetic.main.fragment_intro_button.view.*
import toothpick.ktp.delegate.lazy

class IntroFragmentAddQSTile : Fragment(), SlideBackgroundColorHolder {

    private val activityUtils: ActivityUtils by lazy()
    private val preferences: Preferences by lazy()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_intro_button, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackgroundColor(defaultBackgroundColor)

        val mButton = view.button
        mButton.setBackgroundColor(activityUtils.darkerColor(COLOR_SLIDE_QSTILE, 0.4f))
        mButton.text = getString(R.string.intro_qstile_button)
        mButton.setOnClickListener {
            activityUtils.getAddQSTileDialog().show()
        }

        val mTitle = view.title
        mTitle.text = getString(R.string.intro_qstile_title)
        val mDescription = view.description
        mDescription.text = getString(R.string.intro_qstile_desc)
        val mImage = view.image
        mImage.setImageResource(R.mipmap.img_intro_qstile)
        val mImage2 = view.image2
        mImage2.setImageResource(R.mipmap.img_intro_qstile_2)

        if (preferences.getTileAdded()) {
            mButton.visibility = View.INVISIBLE
        } else {
            mButton.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility()
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_QSTILE

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.main?.setBackgroundColor(backgroundColor)
    }

    private fun updateButtonVisibility() {
        view?.let {
            if (it.button != null) {
                if (preferences.getTileAdded()) {
                    it.button.visibility = View.INVISIBLE
                } else {
                    it.button.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        fun newInstance(): IntroFragmentAddQSTile {
            return IntroFragmentAddQSTile()
        }
    }
}
