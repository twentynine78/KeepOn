package fr.twentynine.keepon

import android.animation.Animator
import android.app.Dialog
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
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.System.canWrite
import android.service.quicksettings.TileService
import android.text.Html
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Priority
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import fr.twentynine.keepon.generate.Rate
import fr.twentynine.keepon.glide.GlideApp
import fr.twentynine.keepon.glide.TimeoutIconData
import fr.twentynine.keepon.intro.IntroActivity
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.KeepOnUtils
import fr.twentynine.keepon.utils.Preferences
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_tile_settings.*
import kotlinx.android.synthetic.main.card_main_about.*
import kotlinx.android.synthetic.main.card_main_settings.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.math.hypot
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    data class TimeoutSwitch(val switch: SwitchMaterial, val timeoutValue: Int)

    private lateinit var missingSettingsDialog: Dialog
    private lateinit var permissionDialog: Dialog
    private lateinit var notificationDialog: Dialog
    private lateinit var rate: Rate
    private lateinit var snackbar: Snackbar

    private var receiverRegistered = false

    // Define default and max size of views and coefficient for bottomsheet slide
    private val defaultPreviewSize = 60.px
    private val defaultPreviewPadding = 14.px
    private val coefficientStartMarginPeek = 1.1
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
                // A hack to prevent a private serializable classloader attack
                if (BundleScrubber.scrub(intent)) {
                    finish()
                    return
                }

                when (intent.action) {
                    ACTION_UPDATE_UI -> {
                        // Update all switch from saved preference
                        updateSwitchs(getTimeoutSwitchsArray())

                        // Set tile preview Image View
                        updateTilePreview()
                    }
                    ACTION_MISSING_SETTINGS -> {
                        // Show missing settings dialog
                        if (Preferences.getSelectedTimeout(context!!).size <= 1) {
                            if (!missingSettingsDialog.isShowing) {
                                missingSettingsDialog.show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A hack to prevent a private serializable classloader attack
        if (BundleScrubber.scrub(intent)) {
            finish()
            return
        }

        // Ignore implicit intents, because they are not valid.
        if (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component) {
            finish()
            return
        }

        if (!Preferences.getSkipIntro(this)) {
            // Start SplashScreen
            val splashIntent = SplashScreen.newIntent(this.applicationContext)
            splashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(splashIntent)
            finish()
            return
        }

        // Set DarkTheme
        if (Preferences.getDarkTheme(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Create Snackbar for saving settings
        snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.settings_save), Snackbar.LENGTH_LONG)
            .setAnchorView(R.id.bottomSheet)

        // Set OnClickListener for each switch
        for (timeoutSwitch: TimeoutSwitch in getTimeoutSwitchsArray()) {
            timeoutSwitch.switch.setOnClickListener { saveSelectedSwitch() }
            timeoutSwitch.switch.setOnLongClickListener {
                KeepOnUtils.getDefaultTimeoutDialog(
                    timeoutSwitch.timeoutValue,
                    timeoutSwitch.switch.text.toString(),
                    this
                ).show()

                true
            }
        }

        // Set OnClickListener to open credits dialog
        card_about_credits_label.setOnClickListener {
            KeepOnUtils.getCreditsDialog(this).show()
        }
        card_about_credits.setOnClickListener {
            KeepOnUtils.getCreditsDialog(this).show()
        }

        // Manage checkbox for monitor screen off or not
        checkBoxScreenOff.isChecked = Preferences.getResetTimeoutOnScreenOff(this)
        checkBoxScreenOff.setOnCheckedChangeListener { _, isChecked ->
            Preferences.setResetTimeoutOnScreenOff(isChecked, this)

            if (!isChecked) {
                KeepOnUtils.stopScreenOffReceiverService(this)
            }

            if (Preferences.getKeepOnState(this) && isChecked) {
                KeepOnUtils.startScreenOffReceiverService(this)
            }
            snackbar.show()
        }

        // Set application version on about card
        var sVersion = getString(R.string.about_card_version)
        sVersion += String.format(Locale.getDefault(), " %s", KeepOnUtils.getAppVersion(this))
        card_about_version.text = Html.fromHtml(sVersion, HtmlCompat.FROM_HTML_MODE_LEGACY)

        animateCardView()

        // Create Dialogs
        missingSettingsDialog = KeepOnUtils.getMissingSettingsDialog(this)
        permissionDialog = KeepOnUtils.getPermissionDialog(this, MainActivity::class.java)
        notificationDialog = KeepOnUtils.getNotificationDialog(this, MainActivity::class.java)

        // Show dialog if missing settings on tile click
        if (intent.extras != null) {
            if (intent.extras!!.getBoolean(KeepOnUtils.TAG_MISSING_SETTINGS, false) &&
                Preferences.getSelectedTimeout(this).size <= 1
            ) {
                if (!missingSettingsDialog.isShowing) {
                    missingSettingsDialog.show()
                }
            }
        }

        // Set OnClick listener for bottom sheet peek views
        bottomSheetPeekTextView.setOnClickListener {
            if (BottomSheetBehavior.from(bottomSheet).state == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        // Set OnClick listener for Tile Preview to switch like from Quick Settings
        tilePreview.setOnClickListener {
            if (Preferences.getSelectedTimeout(this).size < 1) {
                KeepOnUtils.sendBroadcastMissingSettings(this)
            }

            Preferences.setTimeout(Preferences.getNextTimeoutValue(this), this)
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
        BottomSheetBehavior.from(bottomSheet).addBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    setOnSlideBottomSheetAnim(slideOffset)
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (snackbar.isShown) snackbar.dismiss()
                }
            })

        // Set initial bottom sheet state
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED

        // Load QS Style preference
        loadQSStylePreferences()

        // Build Generate snackbar
        rate = Rate.Builder(this)
            .setFeedbackAction(Uri.parse(SUPPORT_URI))
            .setSnackBarParent(cardViewContainer)
            .build()
            .count()
    }

    override fun onResume() {
        super.onResume()

        // Check permission to write settings
        if (canWrite(this)) {
            // Show Dialog to disable notifications if enabled
            if (KeepOnUtils.isNotificationEnabled(this)) {
                if (!notificationDialog.isShowing) {
                    notificationDialog.show()
                }
            }

            registerBroadcastReceiver()

            // Start service to monitor screen timeout
            KeepOnUtils.startScreenTimeoutObserverService(this)

            // Update all switch from saved preference
            updateSwitchs(getTimeoutSwitchsArray())

            // Set tile preview Image View
            updateTilePreview()

            // Show Genrate snackbar
            rate.showRequest()
        } else {
            // Show permission Dialog
            if (!permissionDialog.isShowing) {
                permissionDialog.show()
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
                val isDarkTheme = Preferences.getDarkTheme(this)

                Preferences.setDarkTheme(!isDarkTheme, this)
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
            TimeoutSwitch(switch15s, Preferences.getTimeoutValueArray()[0]),
            TimeoutSwitch(switch30s, Preferences.getTimeoutValueArray()[1]),
            TimeoutSwitch(switch1m, Preferences.getTimeoutValueArray()[2]),
            TimeoutSwitch(switch2m, Preferences.getTimeoutValueArray()[3]),
            TimeoutSwitch(switch5m, Preferences.getTimeoutValueArray()[4]),
            TimeoutSwitch(switch10m, Preferences.getTimeoutValueArray()[5]),
            TimeoutSwitch(switch30m, Preferences.getTimeoutValueArray()[6]),
            TimeoutSwitch(switch1h, Preferences.getTimeoutValueArray()[7]),
            TimeoutSwitch(switchInfinite, Preferences.getTimeoutValueArray()[8])
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
        val currentTimeout = Preferences.getCurrentTimeout(this)

        GlideApp.with(this)
            .asBitmap()
            .priority(Priority.HIGH)
            .load(TimeoutIconData(currentTimeout, 1, KeepOnUtils.getIconStyleSignature(this)))
            .into(object : CustomTarget<Bitmap>(150.px, 150.px) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    tilePreview.setImageBitmap(resource)
                    tilePreview.imageTintMode = PorterDuff.Mode.SRC_IN
                    val layerDrawableCircle: LayerDrawable = tilePreviewBackground.drawable as LayerDrawable
                    val circleBackgroundShape = layerDrawableCircle.findDrawableByLayerId(R.id.shape_circle_background) as GradientDrawable

                    if (Preferences.getCurrentTimeout(this@MainActivity) == Preferences.getOriginalTimeout(
                            this@MainActivity
                        )
                    ) {
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
        for (timeoutSwitch in switchsArray) {
            val switch = timeoutSwitch.switch
            val timeout = timeoutSwitch.timeoutValue
            val selectedSwitch = Preferences.getSelectedTimeout(this)
            val originalTimeout = Preferences.getOriginalTimeout(this)
            val currentTimeout = Preferences.getCurrentTimeout(this)

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
                checkBoxScreenOff.text = String.format(
                    Locale.getDefault(),
                    getString(R.string.reset_checkbox),
                    switch.text.toString().toLowerCase(Locale.getDefault())
                )
            } else {
                switch.isClickable = true
                switch.isEnabled = true
                switch.setTextColor(getColor(R.color.colorText))
            }

            if (currentTimeout == timeout) {
                switch.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            } else {
                switch.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
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

    private fun getListIntFromSwitch(): ArrayList<Int> {
        val resultList: ArrayList<Int> = ArrayList()

        for (timeoutSwitch in getTimeoutSwitchsArray()) {
            if (timeoutSwitch.switch.isChecked && timeoutSwitch.timeoutValue != Preferences.getOriginalTimeout(this)) {
                resultList.add(timeoutSwitch.timeoutValue)
            }
        }

        return resultList
    }

    private fun saveSelectedSwitch() {
        Preferences.setSelectedTimeout(getListIntFromSwitch(), this)

        snackbar.show()

        // Update App shortcuts
        lifecycleScope.launch(Dispatchers.Default) {
            delay(1000)
            withTimeout(60000) {
                KeepOnUtils.manageAppShortcut(this@MainActivity)
            }
        }
    }

    private fun animateCardView() {
        cardViewContainer.post {
            if (cardViewContainer.isAttachedToWindow) {
                processCardViewAnim(selectionCard!!, 0)
                processCardViewAnim(aboutCard!!, ANIMATION_DURATION)
            }
        }
    }

    private fun processCardViewAnim(cardView: CardView, startDelay: Long) {
        val cx = cardView.width / 2
        val cy = cardView.height / 2

        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
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
        tilePreview.layoutParams.width = defaultPreviewSize + (slideOffset * (maxPreviewSize - defaultPreviewSize).dp).roundToInt().px
        tilePreview.layoutParams.height = defaultPreviewSize + (slideOffset * (maxPreviewSize - defaultPreviewSize).dp).roundToInt().px

        // Set padding to tile preview image view
        val newPadding = defaultPreviewPadding + (slideOffset * (maxPreviewPadding - defaultPreviewPadding).dp).roundToInt().px
        tilePreview.setPadding(newPadding, newPadding, newPadding, newPadding)

        // Adapt bottom sheet text view left margin
        val params: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            bottomSheetPeekTextView.layoutParams
        )
        params.setMargins(
            (10.px + ((slideOffset * (maxPreviewSize - defaultPreviewSize).dp * coefficientStartMarginPeek))).roundToInt().px,
            10.px,
            10.px,
            10.px
        )
        bottomSheetPeekTextView.layoutParams = params

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

    private fun loadQSStylePreferences() {
        // Load value from preferences
        slider_size.value = Preferences.getQSStyleFontSize(this).toFloat()
        seek_skew.value = Preferences.getQSStyleFontSkew(this).toFloat()
        seek_space.value = Preferences.getQSStyleFontSpacing(this).toFloat()

        radio_typeface_san_serif.isChecked = Preferences.getQSStyleTypefaceSansSerif(this)
        radio_typeface_serif.isChecked = Preferences.getQSStyleTypefaceSerif(this)
        radio_typeface_monospace.isChecked = Preferences.getQSStyleTypefaceMonospace(this)

        radio_style_fill.isChecked = Preferences.getQSStyleTextFill(this)
        radio_style_fill_stroke.isChecked = Preferences.getQSStyleTextFillStroke(this)
        radio_style_stroke.isChecked = Preferences.getQSStyleTextStroke(this)

        switch_fake_bold.isChecked = Preferences.getQSStyleFontBold(this)
        switch_underline.isChecked = Preferences.getQSStyleFontUnderline(this)
        switch_smcp.isChecked = Preferences.getQSStyleFontSMCP(this)

        // Set OnClickListener and OnSeekBarChangeListener for QS Style controls
        val qsStyleOnChangeListener = Slider.OnChangeListener { _, _, _ -> saveQSStyleSlidePreferences() }
        val qsStyleOnclickListener = View.OnClickListener { saveQSStyleClickPreferences() }
        val qsStyleOnclickListenerTypeface = View.OnClickListener {
            if (radio_typeface_san_serif.isChecked) {
                switch_smcp.isEnabled = true
                switch_smcp.isChecked = Preferences.getQSStyleFontSMCP(this)
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
        Preferences.setQSStyleFontSize(slider_size.value.toInt(), this)
        Preferences.setQSStyleFontSkew(seek_skew.value.toInt(), this)
        Preferences.setQSStyleFontSpacing(seek_space.value.toInt(), this)

        // Update Tile Preview
        updateTilePreview()

        // Update QS Tile
        if (Preferences.getTileAdded(this)) {
            lifecycleScope.launch(Dispatchers.Default) {
                delay(500)
                withTimeout(
                    60000
                ) {
                    TileService.requestListeningState(
                        this@MainActivity,
                        ComponentName(applicationContext, KeepOnTileService::class.java)
                    )
                }
            }
        }

        // Update App shortcuts
        lifecycleScope.launch(Dispatchers.Default) {
            delay(1000)
            withTimeout(60000) {
                KeepOnUtils.manageAppShortcut(this@MainActivity)
            }
        }
    }

    private fun saveQSStyleClickPreferences() {
        // Save all values to Preferences
        Preferences.setQSStyleTextFill(radio_style_fill.isChecked, this)
        Preferences.setQSStyleTextFillStroke(radio_style_fill_stroke.isChecked, this)
        Preferences.setQSStyleTextStroke(radio_style_stroke.isChecked, this)

        Preferences.setQSStyleFontBold(switch_fake_bold.isChecked, this)
        Preferences.setQSStyleFontUnderline(switch_underline.isChecked, this)
        if (switch_smcp.isEnabled) {
            Preferences.setQSStyleFontSMCP(switch_smcp.isChecked, this)
        }

        Preferences.setQSStyleTypefaceSansSerif(radio_typeface_san_serif.isChecked, this)
        Preferences.setQSStyleTypefaceSerif(radio_typeface_serif.isChecked, this)
        Preferences.setQSStyleTypefaceMonospace(radio_typeface_monospace.isChecked, this)

        // Update Tile Preview
        updateTilePreview()

        // Update QS Tile
        if (Preferences.getTileAdded(this)) {
            lifecycleScope.launch(Dispatchers.Default) {
                delay(500)
                withTimeout(
                    60000
                ) {
                    TileService.requestListeningState(
                        this@MainActivity,
                        ComponentName(applicationContext, KeepOnTileService::class.java)
                    )
                }
            }
        }

        // Update App shortcuts
        lifecycleScope.launch(Dispatchers.Default) {
            delay(1000)
            withTimeout(60000) {
                KeepOnUtils.manageAppShortcut(this@MainActivity)
            }
        }
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
