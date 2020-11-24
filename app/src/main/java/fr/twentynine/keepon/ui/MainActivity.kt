package fr.twentynine.keepon.ui

import android.animation.Animator
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
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
import androidx.constraintlayout.widget.ConstraintLayout
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
import fr.twentynine.keepon.databinding.ActivityMainBinding
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.ui.intro.IntroActivity
import fr.twentynine.keepon.utils.ActivityUtils
import fr.twentynine.keepon.utils.CommonUtils
import fr.twentynine.keepon.utils.Rate
import fr.twentynine.keepon.utils.glide.TimeoutIconData
import fr.twentynine.keepon.utils.preferences.Preferences
import fr.twentynine.keepon.utils.px
import fr.twentynine.keepon.utils.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import toothpick.ktp.delegate.lazy
import java.util.Formatter
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.math.hypot
import kotlin.math.roundToInt

@Suppress("TooManyFunctions")
class MainActivity : AppCompatActivity() {

    private data class TimeoutSwitch(val switch: SwitchMaterial, val timeoutValue: Int)

    private val activityUtils: ActivityUtils by lazy()
    private val commonUtils: CommonUtils by lazy()
    private val preferences: Preferences by lazy()
    private val rate: Rate by lazy()
    private val glideApp: RequestManager by lazy()

    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::inflate)

    private val snackbar: Snackbar by lazy {
        Snackbar.make(binding.root, getString(R.string.settings_save), Snackbar.LENGTH_LONG)
            .setAnchorView(binding.includeBottomSheet.bottomSheet)
    }

    // Define default and max size of views and coefficient for bottomsheet slide
    private val defaultPreviewSize = 62.px
    private val defaultPreviewPadding = 14.px
    private var maxPreviewSize = 110.px
    private var maxPreviewPadding = defaultPreviewPadding + ((maxPreviewSize - defaultPreviewSize) / 7)
    private var defaultBottomMarginViewHeight = ((maxPreviewSize - defaultPreviewSize) / 4) + 1.px

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    CommonUtils.ACTION_MAIN_ACTIVITY_UPDATE_UI -> {
                        updateUI()
                    }
                    CommonUtils.ACTION_MAIN_ACTIVITY_MISSING_SETTINGS -> {
                        missingSettings()
                    }
                }
            }
        }
    }

    private var dialogSetOriginalTimeoutValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ignore implicit intents, because they are not valid.
        if (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component) {
            finish()
            return
        }

        // Install Toothpick Activity module and inject
        ToothpickHelper.scopedInjection(this)

        if (!preferences.getSkipIntro() || !preferences.getAppIsLaunched()) {
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

        setSupportActionBar(binding.toolbar)

        // Set OnClickListener for each switch
        for (timeoutSwitch: TimeoutSwitch in getTimeoutSwitchsArray()) {
            timeoutSwitch.switch.setOnClickListener { saveSelectedSwitch() }
            timeoutSwitch.switch.setOnLongClickListener {
                dialogSetOriginalTimeoutValue = timeoutSwitch.timeoutValue
                activityUtils.getDefaultTimeoutDialog(
                    timeoutSwitch.timeoutValue,
                    timeoutSwitch.switch.text.toString()
                ).show()

                true
            }
        }

        // Set OnClickListener to open credits dialog
        binding.includeContentMain.includeCardAbout.cardAboutCreditsLabel.setOnClickListener {
            activityUtils.getCreditsDialog().show()
        }
        binding.includeContentMain.includeCardAbout.cardAboutCredits.setOnClickListener {
            activityUtils.getCreditsDialog().show()
        }

        // Manage checkbox for monitor screen off or not
        binding.includeContentMain.includeCardSettings.checkBoxScreenOff.isChecked = preferences.getResetTimeoutOnScreenOff()
        binding.includeContentMain.includeCardSettings.checkBoxScreenOff.setOnCheckedChangeListener { _, isChecked ->
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
        binding.includeContentMain.includeCardAbout.cardAboutVersionTv.text = Html.fromHtml(sVersion.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)

        // Set OnClick listener for bottom sheet peek views
        val bottomSheetStateOnClickListener = View.OnClickListener {
            if (BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheet).state == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        binding.includeBottomSheet.bottomSheetPeekTextView.setOnClickListener { bottomSheetStateOnClickListener.onClick(it) }
        binding.includeBottomSheet.bottomSheetPeekArrow.setOnClickListener { bottomSheetStateOnClickListener.onClick(it) }

        // Set OnClick listener for Tile Preview to switch like from Quick Settings
        binding.includeBottomSheet.tilePreview.setOnClickListener {
            if (preferences.getAppIsLaunched()) {
                if (preferences.getSelectedTimeout().size < 1) {
                    if (preferences.getKeepOnState()) {
                        preferences.setTimeout(preferences.getOriginalTimeout())
                    } else {
                        missingSettings()
                    }
                } else {
                    preferences.setTimeout(preferences.getNextTimeoutValue())
                }
            }
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
        defaultBottomMarginViewHeight = ((maxPreviewSize - defaultPreviewSize) / 4) + 1.px
        binding.includeBottomSheet.bottomMarginView.layoutParams.height = defaultBottomMarginViewHeight
        binding.includeBottomSheet.bottomMarginView.requestLayout()

        // Set onSlide bottom sheet behavior
        BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheet).addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                setOnSlideBottomSheetAnim(slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (snackbar.isShown) snackbar.dismiss()
            }
        })

        // Set BottomSheet to collapsed at launch
        setOnSlideBottomSheetAnim(0.0F)
        BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED

        // Retrieve saved state
        retrieveSavedState(savedInstanceState)

        // Load QS Style preference
        loadQSStylePreferences()

        // Add count to Genrate
        rate.count()

        // Register BroadcastReceiver
        val intentFiler = IntentFilter()
        intentFiler.addAction(CommonUtils.ACTION_MAIN_ACTIVITY_UPDATE_UI)
        intentFiler.addAction(CommonUtils.ACTION_MAIN_ACTIVITY_MISSING_SETTINGS)
        registerReceiver(receiver, intentFiler, "fr.twentynine.keepon.MAIN_BROADCAST_PERMISSION", null)

        setContentView(binding.root)
        animateViews()
    }

    private fun retrieveSavedState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            // Restore BottomSheet state
            if (savedInstanceState.getBoolean(BOTTOM_SHEET_STATE_EXPANDED, false)) {
                lifecycleScope.launch(Dispatchers.Default) {
                    delay(800)
                    launch(Dispatchers.Main) {
                        BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    delay(400)
                }
            }
            // Restore dialog to set original timeout
            if (savedInstanceState.getBoolean(DIALOG_SET_ORIGINAL_TIMEOUT_SHOWED, false)) {
                val dialogTimeout = savedInstanceState.getInt(DIALOG_SET_ORIGINAL_TIMEOUT_VALUE, 0)
                if (dialogTimeout != 0) {
                    commonUtils.getDisplayTimeoutArray()[dialogTimeout]?.let { dialogTextId ->
                        activityUtils.getDefaultTimeoutDialog(dialogTimeout, getString(dialogTextId)).show()
                    }
                }
            }
            // Restore credits dialog
            if (savedInstanceState.getBoolean(DIALOG_CREDITS_SHOWED, false)) {
                activityUtils.getCreditsDialog().show()
            }
        }
        savedInstanceState?.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(DIALOG_SET_ORIGINAL_TIMEOUT_SHOWED, activityUtils.getDefaultTimeoutDialog().isShowing)
        outState.putInt(DIALOG_SET_ORIGINAL_TIMEOUT_VALUE, dialogSetOriginalTimeoutValue)
        outState.putBoolean(DIALOG_CREDITS_SHOWED, activityUtils.getCreditsDialog().isShowing)
        outState.putBoolean(BOTTOM_SHEET_STATE_EXPANDED, BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheet).state == BottomSheetBehavior.STATE_EXPANDED)
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

            // Update all switch from saved preference
            updateSwitchs(getTimeoutSwitchsArray())

            // Set tile preview Image View
            updateTilePreview()

            // Show dialog if missing settings on tile click
            if (intent != null && intent.action != null) {
                if (intent.action == CommonUtils.ACTION_MAIN_ACTIVITY_MISSING_SETTINGS && preferences.getSelectedTimeout().size <= 1) {
                    missingSettings()
                    intent.action = null
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

    override fun onDestroy() {
        unregisterReceiver(receiver)

        super.onDestroy()
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

    override fun onBackPressed() {
        if (BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheet).state == BottomSheetBehavior.STATE_EXPANDED) {
            BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheet).state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            finishAfterTransition()
        }
    }

    private fun updateUI() {
        // Update all switch from saved preference
        updateSwitchs(getTimeoutSwitchsArray())

        // Set tile preview Image View
        updateTilePreview()
    }

    private fun missingSettings() {
        if (canWrite(this) && !activityUtils.isNotificationEnabled() && !activityUtils.getMissingSettingsDialog().isShowing) {
            activityUtils.getMissingSettingsDialog().show()
        }
    }

    private fun getTimeoutSwitchsArray(): ArrayList<TimeoutSwitch> {
        return arrayListOf(
            TimeoutSwitch(binding.includeContentMain.includeCardSettings.switch15s, preferences.getTimeoutValueArray()[0]),
            TimeoutSwitch(binding.includeContentMain.includeCardSettings.switch30s, preferences.getTimeoutValueArray()[1]),
            TimeoutSwitch(binding.includeContentMain.includeCardSettings.switch1m, preferences.getTimeoutValueArray()[2]),
            TimeoutSwitch(binding.includeContentMain.includeCardSettings.switch2m, preferences.getTimeoutValueArray()[3]),
            TimeoutSwitch(binding.includeContentMain.includeCardSettings.switch5m, preferences.getTimeoutValueArray()[4]),
            TimeoutSwitch(binding.includeContentMain.includeCardSettings.switch10m, preferences.getTimeoutValueArray()[5]),
            TimeoutSwitch(binding.includeContentMain.includeCardSettings.switch30m, preferences.getTimeoutValueArray()[6]),
            TimeoutSwitch(binding.includeContentMain.includeCardSettings.switch1h, preferences.getTimeoutValueArray()[7]),
            TimeoutSwitch(binding.includeContentMain.includeCardSettings.switchInfinite, preferences.getTimeoutValueArray()[8])
        )
    }

    private fun updateTilePreview() {
        // Set Bitmap to Tile Preview
        val currentTimeout = preferences.getCurrentTimeout()
        val originalTimeout = preferences.getOriginalTimeout()

        glideApp
            .asBitmap()
            .priority(Priority.HIGH)
            .load(TimeoutIconData(currentTimeout, 1, commonUtils.getIconStyleSignature()))
            .into(object : CustomTarget<Bitmap>(150.px, 150.px) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.includeBottomSheet.tilePreview.setImageBitmap(resource)
                    binding.includeBottomSheet.tilePreview.imageTintMode = PorterDuff.Mode.SRC_IN
                    val layerDrawableCircle: LayerDrawable = binding.includeBottomSheet.tilePreviewBackground.drawable as LayerDrawable
                    val circleBackgroundShape = layerDrawableCircle.findDrawableByLayerId(R.id.shape_circle_background) as GradientDrawable

                    if (currentTimeout == originalTimeout) {
                        binding.includeBottomSheet.tilePreview.imageTintList = getColorStateList(R.color.colorTilePreviewDisabled)
                        circleBackgroundShape.color = ColorStateList.valueOf(getColor(R.color.colorTilePreviewBackgroundDisabled))
                    } else {
                        binding.includeBottomSheet.tilePreview.imageTintList = getColorStateList(R.color.colorTilePreview)
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
            val currentTimeout = preferences.getCurrentTimeout()
            val originalTimeout = preferences.getOriginalTimeout()

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
                binding.includeContentMain.includeCardSettings.checkBoxScreenOff.text = Formatter().format(
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
        commonUtils.manageAppShortcuts()
    }

    private fun loadQSStylePreferences() {
        // Load value from activityUtils
        binding.includeBottomSheet.sliderSize.value = preferences.getQSStyleFontSize().toFloat()
        binding.includeBottomSheet.seekSkew.value = preferences.getQSStyleFontSkew().toFloat()
        binding.includeBottomSheet.seekSpace.value = preferences.getQSStyleFontSpacing().toFloat()

        binding.includeBottomSheet.radioTypefaceSanSerif.isChecked = preferences.getQSStyleTypefaceSansSerif()
        binding.includeBottomSheet.radioTypefaceSerif.isChecked = preferences.getQSStyleTypefaceSerif()
        binding.includeBottomSheet.radioTypefaceMonospace.isChecked = preferences.getQSStyleTypefaceMonospace()

        binding.includeBottomSheet.radioStyleFill.isChecked = preferences.getQSStyleTextFill()
        binding.includeBottomSheet.radioStyleFillStroke.isChecked = preferences.getQSStyleTextFillStroke()
        binding.includeBottomSheet.radioStyleStroke.isChecked = preferences.getQSStyleTextStroke()

        binding.includeBottomSheet.switchFakeBold.isChecked = preferences.getQSStyleFontBold()
        binding.includeBottomSheet.switchUnderline.isChecked = preferences.getQSStyleFontUnderline()
        binding.includeBottomSheet.switchSmcp.isChecked = preferences.getQSStyleFontSMCP()

        // Set OnClickListener and OnSeekBarChangeListener for QS Style controls
        val qsStyleOnChangeListener = Slider.OnChangeListener { _, _, _ -> saveQSStyleSlidePreferences() }
        val qsStyleOnclickListener = View.OnClickListener { saveQSStyleClickPreferences() }
        val qsStyleOnclickListenerTypeface = View.OnClickListener {
            if (binding.includeBottomSheet.radioTypefaceSanSerif.isChecked) {
                binding.includeBottomSheet.switchSmcp.isEnabled = true
                binding.includeBottomSheet.switchSmcp.isChecked = preferences.getQSStyleFontSMCP()
            } else {
                binding.includeBottomSheet.switchSmcp.isEnabled = false
                binding.includeBottomSheet.switchSmcp.isChecked = false
            }
            saveQSStyleClickPreferences()
        }

        binding.includeBottomSheet.sliderSize.addOnChangeListener(qsStyleOnChangeListener)
        binding.includeBottomSheet.seekSkew.addOnChangeListener(qsStyleOnChangeListener)
        binding.includeBottomSheet.seekSpace.addOnChangeListener(qsStyleOnChangeListener)

        binding.includeBottomSheet.radioStyleFill.setOnClickListener(qsStyleOnclickListener)
        binding.includeBottomSheet.radioStyleFillStroke.setOnClickListener(qsStyleOnclickListener)
        binding.includeBottomSheet.radioStyleStroke.setOnClickListener(qsStyleOnclickListener)

        binding.includeBottomSheet.switchFakeBold.setOnClickListener(qsStyleOnclickListener)
        binding.includeBottomSheet.switchUnderline.setOnClickListener(qsStyleOnclickListener)
        binding.includeBottomSheet.switchSmcp.setOnClickListener(qsStyleOnclickListener)

        binding.includeBottomSheet.radioTypefaceSanSerif.setOnClickListener(qsStyleOnclickListenerTypeface)
        binding.includeBottomSheet.radioTypefaceSerif.setOnClickListener(qsStyleOnclickListenerTypeface)
        binding.includeBottomSheet.radioTypefaceMonospace.setOnClickListener(qsStyleOnclickListenerTypeface)
    }

    private fun saveQSStyleSlidePreferences() {
        preferences.setQSStyleFontSize(binding.includeBottomSheet.sliderSize.value.toInt())
        preferences.setQSStyleFontSkew(binding.includeBottomSheet.seekSkew.value.toInt())
        preferences.setQSStyleFontSpacing(binding.includeBottomSheet.seekSpace.value.toInt())

        // Update Tile Preview
        updateTilePreview()

        // Update QS Tile
        commonUtils.updateQSTile(500)

        // Update App shortcuts
        commonUtils.manageAppShortcuts()
    }

    private fun saveQSStyleClickPreferences() {
        // Save all values to Preferences
        preferences.setQSStyleTextFill(binding.includeBottomSheet.radioStyleFill.isChecked)
        preferences.setQSStyleTextFillStroke(binding.includeBottomSheet.radioStyleFillStroke.isChecked)
        preferences.setQSStyleTextStroke(binding.includeBottomSheet.radioStyleStroke.isChecked)

        preferences.setQSStyleFontBold(binding.includeBottomSheet.switchFakeBold.isChecked)
        preferences.setQSStyleFontUnderline(binding.includeBottomSheet.switchUnderline.isChecked)
        if (binding.includeBottomSheet.switchSmcp.isEnabled) {
            preferences.setQSStyleFontSMCP(binding.includeBottomSheet.switchSmcp.isChecked)
        }

        preferences.setQSStyleTypefaceSansSerif(binding.includeBottomSheet.radioTypefaceSanSerif.isChecked)
        preferences.setQSStyleTypefaceSerif(binding.includeBottomSheet.radioTypefaceSerif.isChecked)
        preferences.setQSStyleTypefaceMonospace(binding.includeBottomSheet.radioTypefaceMonospace.isChecked)

        // Update Tile Preview
        updateTilePreview()

        // Update QS Tile
        commonUtils.updateQSTile(500)

        // Update App shortcuts
        commonUtils.manageAppShortcuts()
    }

    private fun animateViews() {
        binding.includeContentMain.cardViewContainer.post {
            if (binding.includeContentMain.cardViewContainer.isAttachedToWindow) {
                processCardViewAnim(binding.includeContentMain.includeCardSettings.selectionCard, 0)
                processCardViewAnim(binding.includeContentMain.includeCardAbout.aboutCard, ANIMATION_DURATION)
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

        binding.includeBottomSheet.bottomSheet.startAnimation(animBottomSheet)
        binding.includeBottomSheet.tilePreview.startAnimation(animBottomSheetPreview)
        binding.includeBottomSheet.tilePreviewBackground.startAnimation(animBottomSheetPreviewBackground)
        binding.includeBottomSheet.tilePreviewBorder.startAnimation(animBottomSheetPreviewBorder)
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
        val tilePreviewWidth = defaultPreviewSize + (slideOffset * (maxPreviewSize - defaultPreviewSize)).roundToInt()
        binding.includeBottomSheet.tilePreview.layoutParams.width = tilePreviewWidth
        binding.includeBottomSheet.tilePreview.layoutParams.height = defaultPreviewSize + (slideOffset * (maxPreviewSize - defaultPreviewSize)).roundToInt()

        // Set padding to tile preview image view
        val newPadding = defaultPreviewPadding + (slideOffset * (maxPreviewPadding - defaultPreviewPadding)).roundToInt()
        binding.includeBottomSheet.tilePreview.setPadding(newPadding, newPadding, newPadding, newPadding)

        // Adapt bottom sheet text view padding
        binding.includeBottomSheet.bottomSheetPeekTextView.updatePadding((23.px + tilePreviewWidth), 0, 65.px, 7.px)

        // Rotate peek arrow
        binding.includeBottomSheet.bottomSheetPeekArrow.pivotX = (binding.includeBottomSheet.bottomSheetPeekArrow.measuredWidth / 2).toFloat()
        binding.includeBottomSheet.bottomSheetPeekArrow.pivotY = (binding.includeBottomSheet.bottomSheetPeekArrow.measuredHeight / 2).toFloat()
        binding.includeBottomSheet.bottomSheetPeekArrow.rotation = slideOffset * -180

        // Adapt bottom margin view
        binding.includeBottomSheet.bottomMarginView.layoutParams.height = defaultBottomMarginViewHeight - ((slideOffset * (maxPreviewSize - defaultPreviewSize)).roundToInt() / 4)

        // Adapt bottom margin of the guideline view
        val params: ConstraintLayout.LayoutParams = binding.includeBottomSheet.guideline.layoutParams as ConstraintLayout.LayoutParams
        params.topToTop = binding.includeBottomSheet.tilePreview.id
        params.bottomToBottom = binding.includeBottomSheet.tilePreview.id
        params.startToStart = binding.includeBottomSheet.tilePreview.id
        params.endToEnd = binding.includeBottomSheet.tilePreview.id
        params.setMargins(0, 0, 0, 10.px + (slideOffset * (maxPreviewSize - defaultPreviewSize)).roundToInt() / 2)

        // Apply modification
        binding.includeBottomSheet.tilePreview.requestLayout()
        binding.includeBottomSheet.bottomSheetPeekTextView.requestLayout()
        binding.includeBottomSheet.bottomSheetPeekArrow.requestLayout()
        binding.includeBottomSheet.bottomMarginView.requestLayout()
        binding.includeBottomSheet.bottomSheet.requestLayout()
        binding.includeBottomSheet.guideline.requestLayout()
    }

    private fun transitionBottomSheetBackgroundColor(slideOffset: Float) {
        val colorFrom = resources.getColor(R.color.colorBottomSheet, theme)
        val colorTo = resources.getColor(R.color.colorBackgroundCard, theme)

        val layerDrawableBottomSheep: LayerDrawable = binding.includeBottomSheet.bottomSheetBackground.background as LayerDrawable
        val bottomSheetBackgroundShape = layerDrawableBottomSheep.findDrawableByLayerId(R.id.shape_bottom_sheet_background) as GradientDrawable
        bottomSheetBackgroundShape.color = ColorStateList.valueOf(
            interpolateColor(
                slideOffset,
                colorFrom,
                colorTo
            )
        )

        val layerDrawableCircle: LayerDrawable = binding.includeBottomSheet.tilePreviewBackground.drawable as LayerDrawable
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
        const val ANIMATION_DURATION: Long = 300

        const val DIALOG_SET_ORIGINAL_TIMEOUT_SHOWED = "DIALOG_SET_ORIGINAL_TIMEOUT_SHOWED"
        const val DIALOG_SET_ORIGINAL_TIMEOUT_VALUE = "DIALOG_SET_ORIGINAL_TIMEOUT_VALUE"
        const val DIALOG_CREDITS_SHOWED = "DIALOG_CREDITS_SHOWED"
        const val BOTTOM_SHEET_STATE_EXPANDED = "BOTTOM_SHEET_STATE"

        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}
