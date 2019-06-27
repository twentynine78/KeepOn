package fr.twentynine.keepon.intro.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder
import com.github.paolorotolo.appintro.ISlidePolicy
import com.google.android.material.snackbar.Snackbar
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.IntroActivity.Companion.COLOR_SLIDE_PERM
import fr.twentynine.keepon.utils.KeepOnUtils


class IntroFragmentPermission : Fragment(), ISlideBackgroundColorHolder, ISlidePolicy {

    private lateinit var mContext: Context
    private lateinit var mView: View
    private lateinit var mButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContext = context!!
        mView = inflater.inflate(R.layout.fragment_intro_button, container, false)

        setBackgroundColor(defaultBackgroundColor)

        mButton = mView.findViewById(R.id.button)
        mButton.setBackgroundColor(Color.parseColor(KeepOnUtils.darkerColor(COLOR_SLIDE_PERM, 0.4f)))
        mButton.text = getString(R.string.dialog_permission_button)
        mButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:" + mContext.packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            mContext.startActivity(intent)
        }

        val mTitle = mView.findViewById<TextView>(R.id.title)
        mTitle.text = getString(R.string.dialog_permission_title)
        val mDescription = mView.findViewById<TextView>(R.id.description)
        mDescription.text = getString(R.string.dialog_permission_text)
        val mImage = mView.findViewById<ImageView>(R.id.image)
        mImage.setImageResource(R.mipmap.img_intro_perm)

        return mView
    }

    override fun onResume() {
        super.onResume()
        if (Settings.System.canWrite(context!!.applicationContext))
            mButton.visibility = View.INVISIBLE
        else
            mButton.visibility = View.VISIBLE
    }

    override fun onUserIllegallyRequestedNextPage() {
        return Snackbar.make(mView, getString(R.string.intro_toast_permission_needed), Snackbar.LENGTH_LONG).show()
    }

    override fun isPolicyRespected(): Boolean {
        return Settings.System.canWrite(context!!.applicationContext)
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        val constraintLayout = mView.findViewById<ConstraintLayout>(R.id.main)
        constraintLayout.setBackgroundColor(backgroundColor)
    }

    override fun getDefaultBackgroundColor(): Int {
        return Color.parseColor(COLOR_SLIDE_PERM)
    }
}
