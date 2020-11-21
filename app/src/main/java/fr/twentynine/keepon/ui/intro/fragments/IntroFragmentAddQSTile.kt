package fr.twentynine.keepon.ui.intro.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import fr.twentynine.keepon.KeepOnApplication.Companion.viewBinding
import fr.twentynine.keepon.R
import fr.twentynine.keepon.databinding.FragmentIntroButtonBinding
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity.Companion.COLOR_SLIDE_QSTILE
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy

class IntroFragmentAddQSTile : Fragment(R.layout.fragment_intro_button), SlideBackgroundColorHolder {

    private val activityUtils: ActivityUtils by lazy()
    private val preferences: Preferences by lazy()

    private val binding: FragmentIntroButtonBinding by viewBinding(FragmentIntroButtonBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackgroundColor(defaultBackgroundColor)

        val mButton = binding.button
        mButton.setBackgroundColor(activityUtils.darkerColor(COLOR_SLIDE_QSTILE, 0.4f))
        mButton.text = getString(R.string.intro_qstile_button)
        mButton.setOnClickListener {
            activityUtils.getAddQSTileDialog().show()
        }

        val mTitle = binding.title
        mTitle.text = getString(R.string.intro_qstile_title)
        val mDescription = binding.description
        mDescription.text = getString(R.string.intro_qstile_desc)
        val mImage = binding.image
        mImage.setImageResource(R.mipmap.img_intro_qstile)
        val mImage2 = binding.image2
        mImage2.setImageResource(R.mipmap.img_intro_qstile_2)

        if (preferences.getTileAdded()) {
            mButton.visibility = View.GONE
        } else {
            mButton.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        if (preferences.getTileAdded()) {
            binding.button.visibility = View.GONE
        } else {
            binding.button.visibility = View.VISIBLE
        }
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_QSTILE

    override fun setBackgroundColor(backgroundColor: Int) {
        binding.main.setBackgroundColor(backgroundColor)
    }

    companion object {
        fun newInstance(): IntroFragmentAddQSTile {
            return IntroFragmentAddQSTile()
        }
    }
}
