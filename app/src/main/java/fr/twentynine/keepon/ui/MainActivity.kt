package fr.twentynine.keepon.ui

import android.animation.Animator
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.provider.Settings.System.canWrite
import android.text.Html
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.Rate
import fr.twentynine.keepon.utils.glide.TimeoutIconData
import fr.twentynine.keepon.utils.preferences.Preferences
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_tile_settings.*
import kotlinx.android.synthetic.main.card_main_about.*
import kotlinx.android.synthetic.main.card_main_settings.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import toothpick.ktp.delegate.lazy
import java.util.Formatter
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.math.hypot
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    data class TimeoutSwitch(val switch: SwitchMaterial, val timeoutValue: Int)

    private val activityUtils: ActivityUtils by lazy()
    private val commonUtils: CommonUtils by lazy()
    private val preferences: Preferences by lazy()
    private val rate: Rate by lazy()
    private val glideApp: RequestManager by lazy()

    private val snackbar: Snackbar by lazy {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.settings_save), Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.bottomSheet)
    }

    private var receiverRegistered = false

    // Define default and max size of views and coefficient for bottomsheet slide
    private val defaultPreviewSize = 60.px
    private val defaultPreviewPadding = 14.px
    private var maxPreviewSize = 110.px
    private var maxPreviewPadding = defaultPreviewPadding + ((maxPreviewSize - defaultPreviewSize) / 7)
    private var defaultBottomMarginViewHeight = ((maxPreviewSize - defaultPreviewSize) / 2)

    private val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    ACTION_UPDATE_UI -> {
                        // Update all switch from saved preference
                        updateSwitchs(getTimeoutSwitchsArray())

                        // Set tile preview Image View
                        updateTilePreview()
                    }
                    ACTION_MISSING_SETTINGS -> {
                        // Show missing settings dialog
                        if (preferences.getSelectedTimeout().size <= 1) {
                            if (!activityUtils.getMissingSettingsDialog().isShowing) {
                                activityUtils.getMissingSettingsDialog().show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install Toothpick Activity module and inject
        ToothpickHelper.scopedInjection(this)

        // Ignore implicit intents, because they are not valid.
        if (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component) {
            finish()
            return
        }

        if (!preferences.getSkipIntro()) {
            // Start SplashScreen
            val splashIntent = SplashScreen.newIntent(this.applicationContext)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(splashIntent)
            finish()
            return
        }

        // Set DarkTheme
        if (preferences.getDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Set OnClickListener for each switch
        for (timeoutSwitch: TimeoutSwitch in getTimeoutSwitchsArray()) {
            timeoutSwitch.switch.setOnClickListener { saveSelectedSwitch() }
            timeoutSwitch.switch.setOnLongClickListener {
                activityUtils.getDefaultTimeoutDialog(
                    timeoutSwitch.timeoutValue,
                    timeoutSwitch.switch.text.toString()
                ).show()

                true
            }
        }

        // Set OnClickListener to open credits dialog
        card_about_credits_label.setOnClickListener {
            activityUtils.getCreditsDialog().show()
        }
        card_about_credits.setOnClickListener {
            activityUtils.getCreditsDialog().show()
        }

        // Manage checkbox for monitor screen off or not
        checkBoxScreenOff.isChecked = preferences.getResetTimeoutOnScreenOff()
        checkBoxScreenOff.setOnCheckedChangeListener { _, isChecked ->
            preferences.setResetTimeoutOnScreenOff(isChecked)

            if (!isChecked) {
                commonUtils.stopScreenOffReceiverService()
            }

            if (preferences.getKeepOnState() && isChecked) {
                commonUtils.startScreenOffReceiverService()
            }
            snackbar.show()
        }

        // Set application version on about card
        val sVersion = StringBuilder(getString(R.string.about_card_version))
            .append(" ")
            .append(activityUtils.getAppVersion())
        card_about_version_tv.text = Html.fromHtml(sVersion.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

        animateViews()

        // Show dialog if missing settings on tile click
        if (intent.action != null) {
            if (intent.action == ACTION_MISSING_SETTINGS && preferences.getSelectedTimeout().size <= 1) {
                if (!activityUtils.getMissingSettingsDialog().isShowing) {
                    activityUtils.getMissingSettingsDialog().show()
                }
            }
        }

        // Set OnClick listener for bottom sheet peek views
        val bottomSheetStateOnClickListener = View.OnClickListener {
            if (BottomSheetBehavior.from(bottomSheet).state == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        bottomSheetPeekTextView.setOnClickListener { bottomSheetStateOnClickListener.onClick(it) }
        bottomSheetPeekArrow.setOnClickListener { bottomSheetStateOnClickListener.onClick(it) }

        // Set OnClick listener for Tile Preview to switch like from Quick Settings
        tilePreview.setOnClickListener {
            if (preferences.getSelectedTimeout().size < 1) {
                commonUtils.sendBroadcastMissingSettings()
            }

            preferences.setTimeout(preferences.getNextTimeoutValue())
        }

        // Define tile preview max size and adjust bottomsheet margin view height
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val maxScreenSize = (width / 3)
        if (maxPreviewSize > maxScreenSize) {
            maxPreviewSize = if (maxScreenSize < defaultPreviewSize + 10.px) {
                defaultPreviewSize + 10.px
            } else {
                maxScreenSize
            }
        }
        maxPreviewPadding = defaultPreviewPadding + ((maxPreviewSize - defaultPreviewSize) / 10)
        defaultBottomMarginViewHeight = ((maxPreviewSize - defaultPreviewSize) / 2) + 1.px
        bottomMarginView.layoutParams.height = defaultBottomMarginViewHeight
        bottomMarginView.requestLayout()

        // Set onSlide bottom sheet behavior
        BottomSheetBehavior.from(bottomSheet).addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                setOnSlideBottomSheetAnim(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (snackbar.isShown) snackbar.dismiss()
            }
        })

        // Load QS Style preference
        loadQSStylePreferences()

        // Add count to Genrate
        rate.count()
    }

    override fun onResume() {
        super.onResume()

        // Check permission to write settings
        if (canWrite(this)) {
            // Show Dialog to disable notifications if enabled
            if (activityUtils.isNotificationEnabled()) {
                if (!activityUtils.getNotificationDialog().isShowing) {
                    activityUtils.getNotificationDialog().show()
                }
            }

            registerBroadcastReceiver()

            // Start service to monitor screen timeout
            commonUtils.startScreenTimeoutObserverService()

            // Update all switch from saved preference
            updateSwitchs(getTimeoutSwitchsArray())

            // Set tile preview Image View
            updateTilePreview()

            // Show dialog if missing settings on tile click
            if (intent != null && intent.action != null) {
                if (intent.action == ACTION_MISSING_SETTINGS &&
                    preferences.getSelectedTimeout().size <= 1
                ) {
                    if (!activityUtils.getMissingSettingsDialog().isShowing) {
                        activityUtils.getMissingSettingsDialog().show()
                    }
                }
            }

            // Show Genrate snackbar
            rate.showRequest()
        } else {
            // Show permission Dialog
            if (!activityUtils.getPermissionDialog().isShowing) {
                activityUtils.getPermissionDialog().show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_theme -> {
                val isDarkTheme = preferences.getDarkTheme()

                preferences.setDarkTheme(!isDarkTheme)
                if (isDarkTheme) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    startActivity(newIntent(this@MainActivity))
                }
                return true
            }
            R.id.action_help -> {
                startActivity(IntroActivity.newIntent(this))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        if (receiverRegistered) {
            unregisterReceiver(receiver)
            receiverRegistered = false
        }

        super.onPause()
    }

    override fun onBackPressed() {
        if (BottomSheetBehavior.from(bottomSheet).state == BottomSheetBehavior.STATE_EXPANDED) {
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            finishAfterTransition()
        }
    }

    private fun getTimeoutSwitchsArray(): ArrayList<TimeoutSwitch> {
        return arrayListOf(
            TimeoutSwitch(switch15s, preferences.getTimeoutValueArray()[0]),
            TimeoutSwitch(switch30s, preferences.getTimeoutValueArray()[1]),
            TimeoutSwitch(switch1m, preferences.getTimeoutValueArray()[2]),
            TimeoutSwitch(switch2m, preferences.getTimeoutValueArray()[3]),
            TimeoutSwitch(switch5m, preferences.getTimeoutValueArray()[4]),
            TimeoutSwitch(switch10m, preferences.getTimeoutValueArray()[5]),
            TimeoutSwitch(switch30m, preferences.getTimeoutValueArray()[6]),
            TimeoutSwitch(switch1h, preferences.getTimeoutValueArray()[7]),
            TimeoutSwitch(switchInfinite, preferences.getTimeoutValueArray()[8])
        )
    }

    private fun registerBroadcastReceiver() {
        val intentFiler = IntentFilter()
        intentFiler.addAction(ACTION_UPDATE_UI)
        intentFiler.addAction(ACTION_MISSING_SETTINGS)
        registerReceiver(receiver, intentFiler)
        receiverRegistered = true
    }

    private fun updateTilePreview() {
        // Set Bitmap to Tile Preview
        val currentTimeout = preferences.getCurrentTimeout()

        glideApp
            .asBitmap()
            .priority(Priority.HIGH)
            .load(TimeoutIconData(currentTimeout, 1, commonUtils.getIconStyleSignature()))
            .into(object : CustomTarget<Bitmap>(150.px, 150.px) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    tilePreview.setImageBitmap(resource)
                    tilePreview.imageTintMode = PorterDuff.Mode.SRC_IN
                    val layerDrawableCircle: LayerDrawable = tilePreviewBackground.drawable as LayerDrawable
                    val circleBackgroundShape = layerDrawableCircle.findDrawableByLayerId(R.id.shape_circle_background) as GradientDrawable

                    if (preferences.getCurrentTimeout() == preferences.getOriginalTimeout()) {
                        tilePreview.imageTintList = getColorStateList(R.color.colorTilePreviewDisabled)
                        circleBackgroundShape.color = ColorStateList.valueOf(getColor(R.color.colorTilePreviewBackgroundDisabled))
                    } else {
                        tilePreview.imageTintList = getColorStateList(R.color.colorTilePreview)
                        circleBackgroundShape.color = ColorStateList.valueOf(getColor(R.color.colorTilePreviewBackground))
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    private fun updateSwitchs(switchsArray: ArrayList<TimeoutSwitch>) {
        val normalTypeFace = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val boldTypeFace = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        for (timeoutSwitch in switchsArray) {
            val switch = timeoutSwitch.switch
            val timeout = timeoutSwitch.timeoutValue
            val selectedSwitch = preferences.getSelectedTimeout()
            val originalTimeout = preferences.getOriginalTimeout()
            val currentTimeout = preferences.getCurrentTimeout()

            // Check for DevicePolicy restriction
            val mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            var adminTimeout = mDPM.getMaximumTimeToLock(null)
            if (adminTimeout == 0L) adminTimeout = Long.MAX_VALUE

            switch.isChecked = (selectedSwitch.contains(timeout) || originalTimeout == timeout)

            if (originalTimeout == timeout) {
                switch.isClickable = false
                switch.isEnabled = false
                switch.setTextColor(getColor(R.color.colorTextDisabled))

                // Set original timeout in checkBoxScreenOff text
                checkBoxScreenOff.text = Formatter().format(
                    getString(R.string.reset_checkbox),
                    switch.text.toString().toLowerCase(Locale.getDefault())
                ).toString()
            } else {
                switch.isClickable = true
                switch.isEnabled = true
                switch.setTextColor(getColor(R.color.colorText))
            }

            if (currentTimeout == timeout) {
                switch.typeface = boldTypeFace
            } else {
                switch.typeface = normalTypeFace
            }

            if (adminTimeout < timeout) {
                switch.visibility = View.GONE
                if (switch.isChecked) {
                    switch.isChecked = false
                    saveSelectedSwitch()
                }
            } else {
                switch.visibility = View.VISIBLE
            }
        }
    }

    private fun saveSelectedSwitch() {
        val resultList: ArrayList<Int> = ArrayList()
        for (timeoutSwitch in getTimeoutSwitchsArray()) {
            if (timeoutSwitch.switch.isChecked && timeoutSwitch.timeoutValue != preferences.getOriginalTimeout()) {
                resultList.add(timeoutSwitch.timeoutValue)
            }
        }

        preferences.setSelectedTimeout(resultList)

        snackbar.show()

        // Update App shortcuts
        commonUtils.manageAppShortcut()
    }

    private fun loadQSStylePreferences() {
        // Load value from activityUtils
        slider_size.value = preferences.getQSStyleFontSize().toFloat()
        seek_skew.value = preferences.getQSStyleFontSkew().toFloat()
        seek_space.value = preferences.getQSStyleFontSpacing().toFloat()

        radio_typeface_san_serif.isChecked = preferences.getQSStyleTypefaceSansSerif()
        radio_typeface_serif.isChecked = preferences.getQSStyleTypefaceSerif()
        radio_typeface_monospace.isChecked = preferences.getQSStyleTypefaceMonospace()

        radio_style_fill.isChecked = preferences.getQSStyleTextFill()
        radio_style_fill_stroke.isChecked = preferences.getQSStyleTextFillStroke()
        radio_style_stroke.isChecked = preferences.getQSStyleTextStroke()

        switch_fake_bold.isChecked = preferences.getQSStyleFontBold()
        switch_underline.isChecked = preferences.getQSStyleFontUnderline()
        switch_smcp.isChecked = preferences.getQSStyleFontSMCP()

        // Set OnClickListener and OnSeekBarChangeListener for QS Style controls
        val qsStyleOnChangeListener = Slider.OnChangeListener { _, _, _ -> saveQSStyleSlidePreferences() }
        val qsStyleOnclickListener = View.OnClickListener { saveQSStyleClickPreferences() }
        val qsStyleOnclickListenerTypeface = View.OnClickListener {
            if (radio_typeface_san_serif.isChecked) {
                switch_smcp.isEnabled = true
                switch_smcp.isChecked = preferences.getQSStyleFontSMCP()
            } else {
                switch_smcp.isEnabled = false
                switch_smcp.isChecked = false
            }
            saveQSStyleClickPreferences()
        }

        slider_size.addOnChangeListener(qsStyleOnChangeListener)
        seek_skew.addOnChangeListener(qsStyleOnChangeListener)
        seek_space.addOnChangeListener(qsStyleOnChangeListener)

        radio_style_fill.setOnClickListener(qsStyleOnclickListener)
        radio_style_fill_stroke.setOnClickListener(qsStyleOnclickListener)
        radio_style_stroke.setOnClickListener(qsStyleOnclickListener)

        switch_fake_bold.setOnClickListener(qsStyleOnclickListener)
        switch_underline.setOnClickListener(qsStyleOnclickListener)
        switch_smcp.setOnClickListener(qsStyleOnclickListener)

        radio_typeface_san_serif.setOnClickListener(qsStyleOnclickListenerTypeface)
        radio_typeface_serif.setOnClickListener(qsStyleOnclickListenerTypeface)
        radio_typeface_monospace.setOnClickListener(qsStyleOnclickListenerTypeface)
    }

    private fun saveQSStyleSlidePreferences() {
        preferences.setQSStyleFontSize(slider_size.value.toInt())
        preferences.setQSStyleFontSkew(seek_skew.value.toInt())
        preferences.setQSStyleFontSpacing(seek_space.value.toInt())

        // Update Tile Preview
        updateTilePreview()

        // Update QS Tile
        commonUtils.updateQSTile(500)

        // Update App shortcuts
        commonUtils.manageAppShortcut()
    }

    private fun saveQSStyleClickPreferences() {
        // Save all values to Preferences
        preferences.setQSStyleTextFill(radio_style_fill.isChecked)
        preferences.setQSStyleTextFillStroke(radio_style_fill_stroke.isChecked)
        preferences.setQSStyleTextStroke(radio_style_stroke.isChecked)

        preferences.setQSStyleFontBold(switch_fake_bold.isChecked)
        preferences.setQSStyleFontUnderline(switch_underline.isChecked)
        if (switch_smcp.isEnabled) {
            preferences.setQSStyleFontSMCP(switch_smcp.isChecked)
        }

        preferences.setQSStyleTypefaceSansSerif(radio_typeface_san_serif.isChecked)
        preferences.setQSStyleTypefaceSerif(radio_typeface_serif.isChecked)
        preferences.setQSStyleTypefaceMonospace(radio_typeface_monospace.isChecked)

        // Update Tile Preview
        updateTilePreview()

        // Update QS Tile
        commonUtils.updateQSTile(500)

        // Update App shortcuts
        commonUtils.manageAppShortcut()
    }

    private fun animateViews() {
        cardViewContainer.post {
            if (cardViewContainer.isAttachedToWindow) {
                processCardViewAnim(selectionCard, 0)
                processCardViewAnim(aboutCard, ANIMATION_DURATION)
            }
        }

        val animBottomSheet: Animation = AnimationUtils.loadAnimation(this, R.anim.bottomsheet_translate)
        animBottomSheet.startOffset = 50L

        val animBottomSheetPreview: Animation = AnimationUtils.loadAnimation(this, R.anim.bottomsheet_preview_scale)
        animBottomSheetPreview.startOffset = 400L

        val animBottomSheetPreviewBackground: Animation = AnimationUtils.loadAnimation(this, R.anim.bottomsheet_preview_scale)
        animBottomSheetPreviewBackground.startOffset = 400L

        val animBottomSheetPreviewBorder: Animation = AnimationUtils.loadAnimation(this, R.anim.bottomsheet_preview_scale)
        animBottomSheetPreviewBorder.startOffset = 400L

        bottomSheet.startAnimation(animBottomSheet)
        tilePreview.startAnimation(animBottomSheetPreview)
        tilePreviewBackground.startAnimation(animBottomSheetPreviewBackground)
        tilePreviewBorder.startAnimation(animBottomSheetPreviewBorder)
    }

    private fun processCardViewAnim(cardView: CardView, startDelay: Long) {
        val cx = cardView.left
        val cy = cardView.top

        val finalRadius = hypot(cardView.width.toDouble(), cardView.height.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(cardView, cx, cy, 0f, finalRadius)

        anim.startDelay = startDelay
        anim.duration = ANIMATION_DURATION
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                cardView.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        anim.start()
    }

    private fun setOnSlideBottomSheetAnim(slideOffset: Float) {
        // Change background color
        transitionBottomSheetBackgroundColor(slideOffset)

        // Adapt tile preview image view
        val tilePreviewWidth = defaultPreviewSize + (slideOffset * (maxPreviewSize - defaultPreviewSize).dp).roundToInt().px
        tilePreview.layoutParams.width = tilePreviewWidth
        tilePreview.layoutParams.height = defaultPreviewSize + (slideOffset * (maxPreviewSize - defaultPreviewSize).dp).roundToInt().px

        // Set padding to tile preview image view
        val newPadding = defaultPreviewPadding + (slideOffset * (maxPreviewPadding - defaultPreviewPadding).dp).roundToInt().px
        tilePreview.setPadding(newPadding, newPadding, newPadding, newPadding)

        // Adapt bottom sheet text view padding
        bottomSheetPeekTextView.updatePadding((30.px + tilePreviewWidth), 0, 65.px, 6.px)

        // Rotate peek arrow
        bottomSheetPeekArrow.pivotX = (bottomSheetPeekArrow.measuredWidth / 2).toFloat()
        bottomSheetPeekArrow.pivotY = (bottomSheetPeekArrow.measuredHeight / 2).toFloat()
        bottomSheetPeekArrow.rotation = slideOffset * -180

        // Adapt bottom margin view
        bottomMarginView.layoutParams.height = defaultBottomMarginViewHeight - ((slideOffset * (maxPreviewSize - defaultPreviewSize).dp) / 2).roundToInt().px

        // Apply modification
        tilePreview.requestLayout()
        bottomSheetPeekTextView.requestLayout()
        bottomSheetPeekArrow.requestLayout()
        bottomMarginView.requestLayout()
        bottomSheet.requestLayout()
    }

    private fun transitionBottomSheetBackgroundColor(slideOffset: Float) {
        val colorFrom = resources.getColor(R.color.colorBottomSheet, theme)
        val colorTo = resources.getColor(R.color.colorBackgroundCard, theme)

        val layerDrawableBottomSheep: LayerDrawable = bottomSheetBackground.background as LayerDrawable
        val bottomSheetBackgroundShape = layerDrawableBottomSheep.findDrawableByLayerId(R.id.shape_bottom_sheet_background) as GradientDrawable
        bottomSheetBackgroundShape.color = ColorStateList.valueOf(
            interpolateColor(
                slideOffset,
                colorFrom,
                colorTo
            )
        )

        val layerDrawableCircle: LayerDrawable = tilePreviewBackground.drawable as LayerDrawable
        val circleBackgroundShape = layerDrawableCircle.findDrawableByLayerId(R.id.shape_circle_background) as GradientDrawable
        circleBackgroundShape.setStroke(
            3.px,
            ColorStateList.valueOf(
                interpolateColor(
                    slideOffset,
                    colorFrom,
                    colorTo
                )
            )
        )
    }

    private fun interpolateColor(fraction: Float, startValue: Int, endValue: Int): Int {
        val startA = startValue shr 24 and 0xff
        val startR = startValue shr 16 and 0xff
        val startG = startValue shr 8 and 0xff
        val startB = startValue and 0xff
        val endA = endValue shr 24 and 0xff
        val endR = endValue shr 16 and 0xff
        val endG = endValue shr 8 and 0xff
        val endB = endValue and 0xff
        return startA + (fraction * (endA - startA)).toInt() shl 24 or
            (startR + (fraction * (endR - startR)).toInt() shl 16) or
            (startG + (fraction * (endG - startG)).toInt() shl 8) or
            startB + (fraction * (endB - startB)).toInt()
    }

    companion object {
        const val ACTION_UPDATE_UI = "fr.twentynine.keepon.action.UPDATE_UI"
        const val ACTION_MISSING_SETTINGS = "fr.twentynine.keepon.action.MISSING_SETTINGS"

        const val SUPPORT_URI = "mailto:twentynine78@protonmail.com"

        const val ANIMATION_DURATION: Long = 300

        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}
