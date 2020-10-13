package fr.twentynine.keepon.intro.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.SlidePolicy
import com.google.android.material.snackbar.Snackbar
import fr.twentynine.keepon.R
import fr.twentynine.keepon.intro.IntroActivity
import fr.twentynine.keepon.intro.IntroActivity.Companion.COLOR_SLIDE_PERM
import fr.twentynine.keepon.utils.KeepOnUtils
import kotlinx.android.synthetic.main.fragment_intro_button.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IntroFragmentPermission : Fragment(), SlideBackgroundColorHolder, SlidePolicy {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_intro_button, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackgroundColor(defaultBackgroundColor)

        fun checkPermission() = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            withContext(coroutineContext) {
                delay(500)
                repeat(300) {
                    if (Settings.System.canWrite(requireContext())) {
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
        mButton.setBackgroundColor(KeepOnUtils.darkerColor(COLOR_SLIDE_PERM, 0.4f))
        mButton.text = getString(R.string.dialog_permission_button)
        mButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                .setData(Uri.parse("package:" + requireContext().packageName))
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

            checkPermission()
            requireContext().startActivity(intent)
        }

        val mTitle = view.title
        mTitle.text = getString(R.string.dialog_permission_title)
        val mDescription = view.description
        mDescription.text = getString(R.string.dialog_permission_text)
        val mImage = view.image
        mImage.setImageResource(R.mipmap.img_intro_perm)
        val mImage2 = view.image2
        mImage2.setImageResource(R.mipmap.img_intro_perm_2)

        if (Settings.System.canWrite(requireContext().applicationContext)) {
            mButton.visibility = View.INVISIBLE
        } else {
            mButton.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        val mButton = requireView().button
        if (mButton != null) {
            if (Settings.System.canWrite(requireContext())) {
                mButton.visibility = View.INVISIBLE
            } else {
                mButton.visibility = View.VISIBLE
            }
        }
    }

    override val isPolicyRespected: Boolean
        get() = Settings.System.canWrite(requireContext())

    override fun onUserIllegallyRequestedNextPage() {
        return Snackbar.make(requireView(), getString(R.string.intro_toast_permission_needed), Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.button)
            .show()
    }

    override val defaultBackgroundColor: Int
        get() = COLOR_SLIDE_PERM

    override fun setBackgroundColor(backgroundColor: Int) {
        requireView().main?.setBackgroundColor(backgroundColor)
    }

    companion object {
        fun newInstance(): IntroFragmentPermission {
            return IntroFragmentPermission()
        }
    }
}
