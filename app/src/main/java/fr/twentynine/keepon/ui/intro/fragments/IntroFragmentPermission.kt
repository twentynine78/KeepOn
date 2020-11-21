package fr.twentynine.keepon.ui.intro.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlidePolicy
import com.google.android.material.snackbar.Snackbar
import fr.twentynine.keepon.KeepOnApplication.Companion.viewBinding
import fr.twentynine.keepon.R
import fr.twentynine.keepon.databinding.FragmentIntroButtonBinding
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity.Companion.COLOR_SLIDE_PERM
import fr.twentynine.keepon.utils.ActivityUtils
import toothpick.ktp.delegate.lazy

class IntroFragmentPermission : Fragment(R.layout.fragment_intro_button), SlideBackgroundColorHolder, SlidePolicy {

    private val activityUtils: ActivityUtils by lazy()

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
        mButton.setBackgroundColor(activityUtils.darkerColor(COLOR_SLIDE_PERM, 0.4f))
        mButton.text = getString(R.string.dialog_permission_button)
        mButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:" + requireContext().packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

            activityUtils.checkPermission()
            requireContext().startActivity(intent)
        }

        val mTitle = binding.title
        mTitle.text = getString(R.string.dialog_permission_title)
        val mDescription = binding.description
        mDescription.text = getString(R.string.dialog_permission_text)
        val mImage = binding.image
        mImage.setImageResource(R.mipmap.img_intro_perm)
        val mImage2 = binding.image2
        mImage2.setImageResource(R.mipmap.img_intro_perm_2)

        if (Settings.System.canWrite(requireContext().applicationContext)) {
            mButton.visibility = View.INVISIBLE
        } else {
            mButton.visibility = View.VISIBLE
        }
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
        return Snackbar.make(binding.root, getString(R.string.intro_toast_permission_needed), Snackbar.LENGTH_LONG)
            .setAnchorView(binding.button)
            .show()
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_PERM

    override fun setBackgroundColor(backgroundColor: Int) {
        binding.main.setBackgroundColor(backgroundColor)
    }

    companion object {
        fun newInstance(): IntroFragmentPermission {
            return IntroFragmentPermission()
        }
    }
}
