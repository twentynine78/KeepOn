package fr.twentynine.keepon.intro.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout


class IntroFragmentPermission : Fragment(), SlideBackgroundColorHolder, SlidePolicy {
    private lateinit var mContext: Context
    private lateinit var mView: View
    private lateinit var mButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContext = requireContext()
        mView = inflater.inflate(R.layout.fragment_intro_button, container, false)

        setBackgroundColor(defaultBackgroundColor)

        fun checkSettings() {
            runBlocking {
                if (Settings.System.canWrite(mContext)) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val intent = Intent(mContext, IntroActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
            withTimeout(60000
            ) {
                checkSettings()
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
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            checkSettingOn()
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
            .setAnchorView(R.id.button)
            .show()
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_PERM

    override fun setBackgroundColor(backgroundColor: Int) {
        val constraintLayout = mView.findViewById<ConstraintLayout>(R.id.main)
        constraintLayout.setBackgroundColor(backgroundColor)
    }
}
