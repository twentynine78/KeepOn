package fr.twentynine.keepon.ui.intro.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlidePolicy
import com.github.appintro.SlideSelectionListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity.Companion.COLOR_SLIDE_PERM
import fr.twentynine.keepon.utils.ActivityUtils
import toothpick.ktp.delegate.lazy

class IntroFragmentPermission : Fragment(R.layout.fragment_intro_button), SlideSelectionListener, SlideBackgroundColorHolder, SlidePolicy {

    private val activityUtils: ActivityUtils by lazy()

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

        titleText.text = getString(R.string.dialog_permission_title)
        descriptionText.text = getString(R.string.dialog_permission_text)
        slideImage.setImageResource(R.mipmap.img_intro_perm)
        slideImage2.setImageResource(R.mipmap.img_intro_perm_2)

        button.setBackgroundColor(activityUtils.darkerColor(COLOR_SLIDE_PERM, 0.4f))
        button.text = getString(R.string.dialog_permission_button)
        button.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:" + requireContext().packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

            activityUtils.checkPermission()
            requireContext().startActivity(intent)
        }
        if (Settings.System.canWrite(requireContext().applicationContext)) {
            button.visibility = View.INVISIBLE
        } else {
            button.visibility = View.VISIBLE
        }

        mainLayout.setBackgroundColor(defaultBackgroundColor)

        return view
    }

    override fun onResume() {
        super.onResume()
        if (Settings.System.canWrite(requireContext())) {
            view?.findViewById<MaterialButton>(R.id.button)?.visibility = View.INVISIBLE
        } else {
            view?.findViewById<MaterialButton>(R.id.button)?.visibility = View.VISIBLE
        }
    }

    override val isPolicyRespected: Boolean
        get() {
            val mContext = context
            return if (mContext != null) {
                Settings.System.canWrite(requireContext())
            } else {
                false
            }
        }

    override fun onUserIllegallyRequestedNextPage() {
        view?.let {
            val snackbar = Snackbar.make(it.findViewById(R.id.main), getString(R.string.intro_toast_permission_needed), Snackbar.LENGTH_LONG)
            snackbar.view.layoutParams = activityUtils.getSnackbarLayoutParams(snackbar, it.findViewById<MaterialButton>(R.id.button))
            snackbar.show()
        }
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_PERM

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
    }

    override fun onSlideSelected() {
        activityUtils.setStatusBarColor(defaultBackgroundColor)
        activityUtils.setNavBarColor(defaultBackgroundColor)
    }

    override fun onSlideDeselected() {}

    companion object {
        fun newInstance(): IntroFragmentPermission {
            return IntroFragmentPermission()
        }
    }
}
