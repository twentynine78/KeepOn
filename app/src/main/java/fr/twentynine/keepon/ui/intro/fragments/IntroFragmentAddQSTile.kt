package fr.twentynine.keepon.ui.intro.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlideSelectionListener
import fr.twentynine.keepon.R
import fr.twentynine.keepon.databinding.FragmentIntroButtonBinding
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity.Companion.COLOR_SLIDE_QSTILE
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import fr.twentynine.keepon.utils.viewBinding
import toothpick.ktp.delegate.lazy

class IntroFragmentAddQSTile : Fragment(R.layout.fragment_intro_button), SlideSelectionListener, SlideBackgroundColorHolder {

    private val activityUtils: ActivityUtils by lazy()
    private val preferences: Preferences by lazy()

    private val binding by viewBinding(FragmentIntroButtonBinding::bind)

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_QSTILE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        binding.title.text = getString(R.string.intro_qstile_title)
        binding.description.text = getString(R.string.intro_qstile_desc)
        binding.image.setImageResource(R.mipmap.img_intro_qstile)
        binding.image2.setImageResource(R.mipmap.img_intro_qstile_2)

        binding.button.setBackgroundColor(activityUtils.darkerColor(COLOR_SLIDE_QSTILE, 0.4f))
        binding.button.text = getString(R.string.intro_qstile_button)
        binding.button.setOnClickListener {
            activityUtils.getAddQSTileDialog().show()
        }
        if (preferences.getTileAdded()) {
            binding.button.visibility = View.GONE
        } else {
            binding.button.visibility = View.VISIBLE
        }

        binding.main.setBackgroundColor(defaultBackgroundColor)
    }

    override fun onResume() {
        super.onResume()
        if (preferences.getTileAdded()) {
            binding.button.visibility = View.GONE
        } else {
            binding.button.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        activityUtils.getAddQSTileDialog().dismiss()
        super.onDestroyView()
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        binding.main.setBackgroundColor(backgroundColor)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(DIALOG_QS_HELP_SHOWED, activityUtils.getAddQSTileDialog().isShowing)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(DIALOG_QS_HELP_SHOWED, false)) {
                activityUtils.getAddQSTileDialog().show()
            }
        }
    }

    override fun onSlideSelected() {
        activityUtils.setStatusBarColor(defaultBackgroundColor)
        activityUtils.setNavBarColor(defaultBackgroundColor)
    }

    override fun onSlideDeselected() {}

    companion object {
        internal const val DIALOG_QS_HELP_SHOWED = "DIALOG_QS_HELP"

        fun newInstance(): IntroFragmentAddQSTile {
            return IntroFragmentAddQSTile()
        }
    }
}
