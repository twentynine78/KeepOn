package fr.twentynine.keepon.intro.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
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
import com.google.android.material.snackbar.Snackbar
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.IntroActivity
import fr.twentynine.keepon.intro.IntroActivity.Companion.COLOR_SLIDE_PERM
import fr.twentynine.keepon.utils.KeepOnUtils
import java.lang.Exception


class IntroFragmentPermission constructor(handler: Handler) : Fragment(), SlideBackgroundColorHolder, SlidePolicy {
    private lateinit var mContext: Context
    private lateinit var mView: View
    private lateinit var mButton: Button
    private val mHandler: Handler = handler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContext = requireContext()
        mView = inflater.inflate(R.layout.fragment_intro_button, container, false)

        setBackgroundColor(defaultBackgroundColor)

        val checkSettingOn: Runnable = object : Runnable {
            override fun run() {
                if (Settings.System.canWrite(requireContext().applicationContext)) {
                    val intent = Intent(mContext.applicationContext, IntroActivity::class.java)
                    startActivity(intent)
                    return
                } else {
                    try {
                        mHandler.postDelayed(this, 200)
                    } catch (e: Exception) {
                    }
                }
            }
        }

        mButton = mView.findViewById(R.id.button)
        mButton.setBackgroundColor(KeepOnUtils.darkerColor(COLOR_SLIDE_PERM, 0.4f))
        mButton.text = getString(R.string.dialog_permission_button)
        mButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:" + mContext.packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                mHandler.post(checkSettingOn)
            } catch (e: Exception) {}
            mContext.startActivity(intent)
        }

        val mTitle = mView.findViewById<TextView>(R.id.title)
        mTitle.text = getString(R.string.dialog_permission_title)
        val mDescription = mView.findViewById<TextView>(R.id.description)
        mDescription.text = getString(R.string.dialog_permission_text)
        val mImage = mView.findViewById<ImageView>(R.id.image)
        mImage.setImageResource(R.mipmap.img_intro_perm)

        if (Settings.System.canWrite(requireContext().applicationContext))
            mButton.visibility = View.INVISIBLE
        else
            mButton.visibility = View.VISIBLE

        return mView
    }

    override fun onResume() {
        super.onResume()
        if (Settings.System.canWrite(requireContext().applicationContext))
            mButton.visibility = View.INVISIBLE
        else
            mButton.visibility = View.VISIBLE
    }

    override val isPolicyRespected: Boolean
        get() = Settings.System.canWrite(requireContext().applicationContext)

    override fun onUserIllegallyRequestedNextPage() {
        return Snackbar.make(mView, getString(R.string.intro_toast_permission_needed), Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.bottomSheet)
            .show()
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_PERM

    override fun setBackgroundColor(backgroundColor: Int) {
        val constraintLayout = mView.findViewById<ConstraintLayout>(R.id.main)
        constraintLayout.setBackgroundColor(backgroundColor)
    }
}
