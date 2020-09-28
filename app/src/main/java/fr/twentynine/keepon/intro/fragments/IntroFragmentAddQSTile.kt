package fr.twentynine.keepon.intro.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
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
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.IntroActivity.Companion.COLOR_SLIDE_QSTILE
import fr.twentynine.keepon.utils.KeepOnUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout


class IntroFragmentAddQSTile : Fragment(), SlideBackgroundColorHolder, SlidePolicy {

    private var dismissDialog = false
    private lateinit var dialog: Dialog
    private lateinit var mContext: Context
    private lateinit var mView: View
    private lateinit var mButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContext = requireContext()
        mView = inflater.inflate(R.layout.fragment_intro_button, container, false)

        setBackgroundColor(defaultBackgroundColor)

        fun checkSettings() {
            runBlocking {
                if (KeepOnUtils.getTileAdded(mContext) || dismissDialog) {
                    CoroutineScope(Dispatchers.Main).launch {
                        updateButtonVisibility()
                        dialog.dismiss()
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

        dialog = KeepOnUtils.getAddQSTileDialog(mContext)

        mButton = mView.findViewById(R.id.button)
        mButton.setBackgroundColor(KeepOnUtils.darkerColor(COLOR_SLIDE_QSTILE, 0.4f))
        mButton.text = getString(R.string.intro_qstile_button)
        mButton.setOnClickListener {
            checkSettingOn()
            dialog.setOnDismissListener {
                dismissDialog = true
            }
            dialog.show()
        }

        val mTitle = mView.findViewById<TextView>(R.id.title)
        mTitle.text = getString(R.string.intro_qstile_title)
        val mDescription = mView.findViewById<TextView>(R.id.description)
        mDescription.text = getString(R.string.intro_qstile_desc)
        val mImage = mView.findViewById<ImageView>(R.id.image)
        mImage.setImageResource(R.mipmap.img_intro_qstile)

        if (KeepOnUtils.getTileAdded(mContext))
            mButton.visibility = View.INVISIBLE
        else
            mButton.visibility = View.VISIBLE

        return mView
    }

    override fun onResume() {
        super.onResume()
        updateButtonVisibility()
    }

    override val isPolicyRespected: Boolean
        get() = true

    override fun onUserIllegallyRequestedNextPage() {
        return
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_QSTILE

    override fun setBackgroundColor(backgroundColor: Int) {
        val constraintLayout = mView.findViewById<ConstraintLayout>(R.id.main)
        constraintLayout.setBackgroundColor(backgroundColor)
    }

    private fun updateButtonVisibility() {
        if (KeepOnUtils.getTileAdded(mContext))
            mButton.visibility = View.INVISIBLE
        else
            mButton.visibility = View.VISIBLE
    }
}
