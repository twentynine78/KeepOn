package fr.twentynine.keepon.intro.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder
import com.github.paolorotolo.appintro.ISlidePolicy
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.IntroActivity.Companion.COLOR_SLIDE_NOTIF
import fr.twentynine.keepon.utils.KeepOnUtils


class IntroFragmentNotification : Fragment(), ISlideBackgroundColorHolder, ISlidePolicy {

    private lateinit var mContext: Context
    private lateinit var mView: View
    private lateinit var mButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContext = context!!
        mView = inflater.inflate(R.layout.fragment_intro_button, container, false)

        setBackgroundColor(defaultBackgroundColor)

        mButton = mView.findViewById(R.id.button)
        mButton.setBackgroundColor(Color.parseColor(KeepOnUtils.darkerColor(COLOR_SLIDE_NOTIF, 0.4f)))
        mButton.text = getString(R.string.dialog_notification_button)
        mButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!TextUtils.isEmpty(KeepOnUtils.NOTIFICATION_CHANNEL_ID)) {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, mContext.packageName)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, KeepOnUtils.NOTIFICATION_CHANNEL_ID)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    mContext.startActivity(intent)
                }
            } else {
                val uri = Uri.fromParts("package", mContext.packageName, null)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(uri)
                mContext.startActivity(intent)
            }
        }

        val mTitle = mView.findViewById<TextView>(R.id.title)
        mTitle.text = getString(R.string.dialog_notification_title)
        val mDescription = mView.findViewById<TextView>(R.id.description)
        mDescription.text = getString(R.string.dialog_notification_text)
        val mImage = mView.findViewById<ImageView>(R.id.image)
        mImage.setImageResource(R.mipmap.img_intro_notif)

        return mView
    }

    override fun onResume() {
        super.onResume()
        if (KeepOnUtils.isNotificationEnabled(mContext))
            mButton.visibility = View.VISIBLE
        else
            mButton.visibility = View.INVISIBLE
    }

    override fun onUserIllegallyRequestedNextPage() {
        return
    }

    override fun isPolicyRespected(): Boolean {
        return true
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        val constraintLayout = mView.findViewById<ConstraintLayout>(R.id.main)
        constraintLayout.setBackgroundColor(backgroundColor)
    }

    override fun getDefaultBackgroundColor(): Int {
        return Color.parseColor(COLOR_SLIDE_NOTIF)
    }
}
