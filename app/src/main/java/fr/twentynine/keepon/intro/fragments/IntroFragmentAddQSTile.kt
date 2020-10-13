package fr.twentynine.keepon.intro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlidePolicy
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.IntroActivity.Companion.COLOR_SLIDE_QSTILE
import fr.twentynine.keepon.utils.KeepOnUtils
import fr.twentynine.keepon.utils.Preferences
import kotlinx.android.synthetic.main.fragment_intro_button.view.*

class IntroFragmentAddQSTile : Fragment(), SlideBackgroundColorHolder, SlidePolicy {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_intro_button, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackgroundColor(defaultBackgroundColor)

        val mButton = view.button
        mButton.setBackgroundColor(KeepOnUtils.darkerColor(COLOR_SLIDE_QSTILE, 0.4f))
        mButton.text = getString(R.string.intro_qstile_button)
        mButton.setOnClickListener {
            KeepOnUtils.getAddQSTileDialog(requireContext()).show()
        }

        val mTitle = view.title
        mTitle.text = getString(R.string.intro_qstile_title)
        val mDescription = view.description
        mDescription.text = getString(R.string.intro_qstile_desc)
        val mImage = view.image
        mImage.setImageResource(R.mipmap.img_intro_qstile)
        val mImage2 = view.image2
        mImage2.setImageResource(R.mipmap.img_intro_qstile_2)

        if (Preferences.getTileAdded(requireContext())) {
            mButton.visibility = View.INVISIBLE
        } else {
            mButton.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility()
    }

    override val isPolicyRespected: Boolean
        get() = true

    override fun onUserIllegallyRequestedNextPage() {
        return
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_QSTILE

    override fun setBackgroundColor(backgroundColor: Int) {
        requireView().main.setBackgroundColor(backgroundColor)
    }

    private fun updateButtonVisibility() {
        val mButton = requireView().button
        if (mButton != null) {
            if (Preferences.getTileAdded(requireContext())) {
                mButton.visibility = View.INVISIBLE
            } else {
                mButton.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        fun newInstance(): IntroFragmentAddQSTile {
            return IntroFragmentAddQSTile()
        }
    }
}
