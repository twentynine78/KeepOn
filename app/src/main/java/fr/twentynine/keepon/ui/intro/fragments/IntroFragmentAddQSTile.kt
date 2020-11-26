package fr.twentynine.keepon.ui.intro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlideSelectionListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity.Companion.COLOR_SLIDE_QSTILE
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy

class IntroFragmentAddQSTile : Fragment(), SlideSelectionListener, SlideBackgroundColorHolder {

    private val activityUtils: ActivityUtils by lazy()
    private val preferences: Preferences by lazy()

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_QSTILE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro_button, container, false)
        val titleText = view.findViewById<MaterialTextView>(R.id.title)
        val descriptionText = view.findViewById<MaterialTextView>(R.id.description)
        val slideImage = view.findViewById<ShapeableImageView>(R.id.image)
        val slideImage2 = view.findViewById<ShapeableImageView>(R.id.image2)
        val button = view.findViewById<MaterialButton>(R.id.button)
        val mainLayout = view.findViewById<ConstraintLayout>(R.id.main)

        titleText.text = getString(R.string.intro_qstile_title)
        descriptionText.text = getString(R.string.intro_qstile_desc)
        slideImage.setImageResource(R.mipmap.img_intro_qstile)
        slideImage2.setImageResource(R.mipmap.img_intro_qstile_2)

        button.setBackgroundColor(activityUtils.darkerColor(COLOR_SLIDE_QSTILE, 0.4f))
        button.text = getString(R.string.intro_qstile_button)
        button.setOnClickListener {
            activityUtils.getAddQSTileDialog().show()
        }
        if (preferences.getTileAdded()) {
            button.visibility = View.GONE
        } else {
            button.visibility = View.VISIBLE
        }

        mainLayout.setBackgroundColor(defaultBackgroundColor)

        return view
    }

    override fun onResume() {
        super.onResume()
        if (preferences.getTileAdded()) {
            view?.findViewById<MaterialButton>(R.id.button)?.visibility = View.GONE
        } else {
            view?.findViewById<MaterialButton>(R.id.button)?.visibility = View.VISIBLE
        }
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
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
