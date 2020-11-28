package fr.twentynine.keepon.ui.intro.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlidePolicy
import com.github.appintro.SlideSelectionListener
import com.google.android.material.snackbar.Snackbar
import fr.twentynine.keepon.R
import fr.twentynine.keepon.databinding.FragmentIntroButtonBinding
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity.Companion.COLOR_SLIDE_PERM
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.viewBinding
import toothpick.ktp.delegate.lazy

class IntroFragmentPermission : Fragment(R.layout.fragment_intro_button), SlideSelectionListener, SlideBackgroundColorHolder, SlidePolicy {

    private val activityUtils: ActivityUtils by lazy()

    private val binding by viewBinding(FragmentIntroButtonBinding::bind)

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_PERM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        binding.title.text = getString(R.string.dialog_permission_title)
        binding.description.text = getString(R.string.dialog_permission_text)
        binding.image.setImageResource(R.mipmap.img_intro_perm)
        binding.image2.setImageResource(R.mipmap.img_intro_perm_2)

        binding.button.setBackgroundColor(activityUtils.darkerColor(COLOR_SLIDE_PERM, 0.4f))
        binding.button.text = getString(R.string.dialog_permission_button)
        binding.button.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:" + requireContext().packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

            activityUtils.checkPermission()
            requireContext().startActivity(intent)
        }
        if (Settings.System.canWrite(requireContext().applicationContext)) {
            binding.button.visibility = View.INVISIBLE
        } else {
            binding.button.visibility = View.VISIBLE
        }

        binding.main.setBackgroundColor(defaultBackgroundColor)
    }

    override fun onResume() {
        super.onResume()
        if (Settings.System.canWrite(requireContext())) {
            binding.button.visibility = View.INVISIBLE
        } else {
            binding.button.visibility = View.VISIBLE
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
        val snackbar = Snackbar.make(binding.main, getString(R.string.intro_toast_permission_needed), Snackbar.LENGTH_LONG)
        snackbar.view.layoutParams = activityUtils.getSnackbarLayoutParams(snackbar, binding.button)
        snackbar.show()
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        binding.main.setBackgroundColor(backgroundColor)
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
