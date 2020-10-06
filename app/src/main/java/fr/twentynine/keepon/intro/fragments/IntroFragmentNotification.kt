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
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlidePolicy
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.IntroActivity
import fr.twentynine.keepon.intro.IntroActivity.Companion.COLOR_SLIDE_NOTIF
import fr.twentynine.keepon.utils.KeepOnUtils
import kotlinx.android.synthetic.main.fragment_intro_button.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class IntroFragmentNotification : Fragment(), SlideBackgroundColorHolder, SlidePolicy {

    private var mView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mContext = requireContext()
        mView = inflater.inflate(R.layout.fragment_intro_button, container, false)

        setBackgroundColor(defaultBackgroundColor)

        fun checkSettings() {
            runBlocking {
                if (!KeepOnUtils.isNotificationEnabled(mContext)) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val intent = Intent(mContext, IntroActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    delay(200)
                    CoroutineScope(Dispatchers.Default).launch {
                        checkSettings()
                    }
                }
            }
        }

        fun checkSettingOn() = CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            withTimeout(
                60000
            ) {
                checkSettings()
            }
        }

        val mButton = mView!!.button
        mButton.setBackgroundColor(KeepOnUtils.darkerColor(COLOR_SLIDE_NOTIF, 0.4f))
        mButton.text = getString(R.string.dialog_notification_button)
        mButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!TextUtils.isEmpty(KeepOnUtils.NOTIFICATION_CHANNEL_ID)) {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, mContext.packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, KeepOnUtils.NOTIFICATION_CHANNEL_ID)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                    checkSettingOn()
                    mContext.startActivity(intent)
                }
            } else {
                val uri = Uri.fromParts("package", mContext.packageName, null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(uri)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                checkSettingOn()
                mContext.startActivity(intent)
            }
        }

        val mTitle = mView!!.title
        mTitle.text = getString(R.string.dialog_notification_title)
        val mDescription = mView!!.description
        mDescription.text = getString(R.string.dialog_notification_text)
        val mImage = mView!!.image
        mImage.setImageResource(R.mipmap.img_intro_notif)

        if (KeepOnUtils.isNotificationEnabled(mContext)) {
            mButton.visibility = View.VISIBLE
        } else {
            mButton.visibility = View.INVISIBLE
        }

        return mView
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
        get() = COLOR_SLIDE_NOTIF

    override fun setBackgroundColor(backgroundColor: Int) {
        if (mView != null) {
            val constraintLayout = mView!!.main
            constraintLayout.setBackgroundColor(backgroundColor)
        }
    }
}
