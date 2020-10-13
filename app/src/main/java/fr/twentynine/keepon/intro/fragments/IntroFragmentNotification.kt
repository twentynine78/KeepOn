package fr.twentynine.keepon.intro.fragments

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
import androidx.lifecycle.lifecycleScope
import com.github.appintro.SlideBackgroundColorHolder
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.IntroActivity
import fr.twentynine.keepon.intro.IntroActivity.Companion.COLOR_SLIDE_NOTIF
import fr.twentynine.keepon.utils.KeepOnUtils
import kotlinx.android.synthetic.main.fragment_intro_button.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IntroFragmentNotification : Fragment(), SlideBackgroundColorHolder {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_intro_button, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackgroundColor(defaultBackgroundColor)

        fun checkNotification() = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            withContext(coroutineContext) {
                delay(500)
                repeat(300) {
                    if (!KeepOnUtils.isNotificationEnabled(requireContext())) {
                        try {
                            val intent = Intent(requireContext(), IntroActivity::class.java)
                            startActivity(intent)
                        } finally {
                            return@withContext
                        }
                    } else {
                        delay(200)
                    }
                }
            }
        }

        val mButton = view.button
        mButton.setBackgroundColor(KeepOnUtils.darkerColor(COLOR_SLIDE_NOTIF, 0.4f))
        mButton.text = getString(R.string.dialog_notification_button)
        mButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!TextUtils.isEmpty(KeepOnUtils.NOTIFICATION_CHANNEL_ID)) {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, KeepOnUtils.NOTIFICATION_CHANNEL_ID)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                    checkNotification()
                    requireContext().startActivity(intent)
                }
            } else {
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                checkNotification()
                requireContext().startActivity(intent)
            }
        }

        val mTitle = view.title
        mTitle.text = getString(R.string.dialog_notification_title)
        val mDescription = view.description
        mDescription.text = getString(R.string.dialog_notification_text)
        val mImage = view.image
        mImage.setImageResource(R.mipmap.img_intro_notif)
        val mImage2 = view.image2
        mImage2.setImageResource(R.mipmap.img_intro_notif_2)

        if (KeepOnUtils.isNotificationEnabled(requireContext())) {
            mButton.visibility = View.VISIBLE
        } else {
            mButton.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        val mButton = requireView().button
        if (mButton != null) {
            if (KeepOnUtils.isNotificationEnabled(requireContext())) {
                mButton.visibility = View.VISIBLE
            } else {
                mButton.visibility = View.INVISIBLE
            }
        }
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_NOTIF

    override fun setBackgroundColor(backgroundColor: Int) {
        requireView().main?.setBackgroundColor(backgroundColor)
    }

    companion object {
        fun newInstance(): IntroFragmentNotification {
            return IntroFragmentNotification()
        }
    }
}
