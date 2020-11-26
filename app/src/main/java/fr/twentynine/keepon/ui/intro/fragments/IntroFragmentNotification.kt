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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlideSelectionListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity.Companion.COLOR_SLIDE_NOTIF
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.ServiceUtils
import toothpick.ktp.delegate.lazy

class IntroFragmentNotification : Fragment(), SlideSelectionListener, SlideBackgroundColorHolder {

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

        button.setBackgroundColor(activityUtils.darkerColor(COLOR_SLIDE_NOTIF, 0.4f))
        button.text = getString(R.string.dialog_notification_button)
        button.setOnClickListener {
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

        titleText.text = getString(R.string.dialog_notification_title)
        descriptionText.text = getString(R.string.dialog_notification_text)
        slideImage.setImageResource(R.mipmap.img_intro_notif)
        slideImage2.setImageResource(R.mipmap.img_intro_notif_2)

        if (activityUtils.isNotificationEnabled()) {
            button.visibility = View.VISIBLE
        } else {
            button.visibility = View.INVISIBLE
        }

        mainLayout.setBackgroundColor(defaultBackgroundColor)

        return view
    }

    override fun onResume() {
        super.onResume()
        if (activityUtils.isNotificationEnabled()) {
            view?.findViewById<MaterialButton>(R.id.button)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<MaterialButton>(R.id.button)?.visibility = View.INVISIBLE
        }
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_NOTIF

    override fun setBackgroundColor(backgroundColor: Int) {
        view?.findViewById<ConstraintLayout>(R.id.main)?.setBackgroundColor(backgroundColor)
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
