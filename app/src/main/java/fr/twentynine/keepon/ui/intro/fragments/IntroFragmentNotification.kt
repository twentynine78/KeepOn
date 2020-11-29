package fr.twentynine.keepon.ui.intro.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlideSelectionListener
import fr.twentynine.keepon.R
import fr.twentynine.keepon.databinding.FragmentIntroButtonBinding
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity.Companion.COLOR_SLIDE_NOTIF
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.ServiceUtils
import fr.twentynine.keepon.utils.viewBinding
import toothpick.ktp.delegate.lazy

class IntroFragmentNotification : Fragment(R.layout.fragment_intro_button), SlideSelectionListener, SlideBackgroundColorHolder {

    private val activityUtils: ActivityUtils by lazy()

    private val binding by viewBinding(FragmentIntroButtonBinding::bind)

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_NOTIF

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        binding.button.setBackgroundColor(activityUtils.darkerColor(COLOR_SLIDE_NOTIF, 0.4f))
        binding.button.text = getString(R.string.dialog_notification_button)
        binding.button.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!TextUtils.isEmpty(ServiceUtils.NOTIFICATION_CHANNEL_ID)) {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, ServiceUtils.NOTIFICATION_CHANNEL_ID)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                    activityUtils.checkNotification()
                    requireContext().startActivity(intent)
                }
            } else {
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                activityUtils.checkNotification()
                requireContext().startActivity(intent)
            }
        }

        binding.title.text = getString(R.string.dialog_notification_title)
        binding.description.text = getString(R.string.dialog_notification_text)
        binding.image.setImageResource(R.mipmap.img_intro_notif)
        binding.image2.setImageResource(R.mipmap.img_intro_notif_2)

        if (activityUtils.isNotificationEnabled()) {
            binding.button.visibility = View.VISIBLE
        } else {
            binding.button.visibility = View.INVISIBLE
        }

        binding.main.setBackgroundColor(defaultBackgroundColor)
    }

    override fun onResume() {
        super.onResume()
        if (activityUtils.isNotificationEnabled()) {
            binding.button.visibility = View.VISIBLE
        } else {
            binding.button.visibility = View.INVISIBLE
        }
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
        fun newInstance(): IntroFragmentNotification {
            return IntroFragmentNotification()
        }
    }
}
