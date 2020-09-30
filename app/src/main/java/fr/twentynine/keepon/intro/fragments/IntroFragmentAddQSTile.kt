package fr.twentynine.keepon.intro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlidePolicy
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.IntroActivity.Companion.COLOR_SLIDE_QSTILE
import fr.twentynine.keepon.utils.KeepOnUtils


class IntroFragmentAddQSTile : Fragment(), SlideBackgroundColorHolder, SlidePolicy {

    private var mView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mContext = requireContext()
        mView = inflater.inflate(R.layout.fragment_intro_button, container, false)

        setBackgroundColor(defaultBackgroundColor)

        val mButton = mView!!.findViewById<Button>(R.id.button)
        mButton.setBackgroundColor(KeepOnUtils.darkerColor(COLOR_SLIDE_QSTILE, 0.4f))
        mButton.text = getString(R.string.intro_qstile_button)
        mButton.setOnClickListener {
            KeepOnUtils.getAddQSTileDialog(mContext).show()
        }

        val mTitle = mView!!.findViewById<TextView>(R.id.title)
        mTitle.text = getString(R.string.intro_qstile_title)
        val mDescription = mView!!.findViewById<TextView>(R.id.description)
        mDescription.text = getString(R.string.intro_qstile_desc)
        val mImage = mView!!.findViewById<ImageView>(R.id.image)
        mImage.setImageResource(R.mipmap.img_intro_qstile)

        if (KeepOnUtils.getTileAdded(mContext))
            mButton.visibility = View.INVISIBLE
        else
            mButton.visibility = View.VISIBLE

        return mView
    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mView = null
    }

    override val isPolicyRespected: Boolean
        get() = true

    override fun onUserIllegallyRequestedNextPage() {
        return
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_QSTILE

    override fun setBackgroundColor(backgroundColor: Int) {
        if (mView != null) {
            val constraintLayout = mView!!.findViewById<ConstraintLayout>(R.id.main)
            constraintLayout.setBackgroundColor(backgroundColor)
        }
    }

    private fun updateButtonVisibility() {
        val mButton = requireView().findViewById<Button>(R.id.button)
        if (mButton != null) {
            if (KeepOnUtils.getTileAdded(requireContext())) {
                mButton.visibility = View.INVISIBLE
            } else {
                mButton.visibility = View.VISIBLE
            }
        }
    }
}
