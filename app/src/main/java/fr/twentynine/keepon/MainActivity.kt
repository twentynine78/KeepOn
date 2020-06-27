package fr.twentynine.keepon

import android.animation.Animator
import android.app.Dialog
import android.app.admin.DevicePolicyManager
import android.content.*
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings.System.canWrite
import android.service.quicksettings.TileService
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar
import fr.twentynine.keepon.services.KeepOnTileService
import fr.twentynine.keepon.services.ScreenOffReceiverService
import fr.twentynine.keepon.utils.KeepOnUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.hypot


class MainActivity : AppCompatActivity() {
    data class TimeoutSwitch(val switch: Switch, val timeoutValue: Int)

    private val animDuration: Long = 300
    private lateinit var timeoutSwitchs: Array<TimeoutSwitch>
    private lateinit var receiver: BroadcastReceiver
    private var screenOffCheckBox: CheckBox? = null
    private var selectionCard: CardView? = null
    private var aboutCard: CardView? = null
    private var missingSettingsDialog: Dialog? = null
    private var permissionDialog: Dialog? = null
    private var notificationDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerBroadcastReceiver(this)

        // Set DarkTheme
        if (KeepOnUtils.getDarkTheme(this))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        selectionCard = this.findViewById(R.id.selectionCard)
        aboutCard = this.findViewById(R.id.aboutCard)

        // Create an array of TimeoutSwitch
        timeoutSwitchs = arrayOf (
            TimeoutSwitch(this.findViewById(R.id.switch15s), 15000),
            TimeoutSwitch(this.findViewById(R.id.switch30s), 30000),
            TimeoutSwitch(this.findViewById(R.id.switch1m), 60000),
            TimeoutSwitch(this.findViewById(R.id.switch2m), 120000),
            TimeoutSwitch(this.findViewById(R.id.switch5m), 300000),
            TimeoutSwitch(this.findViewById(R.id.switch10m), 600000),
            TimeoutSwitch(this.findViewById(R.id.switch30m), 1800000),
            TimeoutSwitch(this.findViewById(R.id.switch1h), 3600000),
            TimeoutSwitch(this.findViewById(R.id.switchInfinite), Int.MAX_VALUE)
        )

        // Set OnClickListener for each switch
        for (timeoutSwitch: TimeoutSwitch in timeoutSwitchs) {
            timeoutSwitch.switch.setOnClickListener { view ->
                saveSelectedSwitch(view)
            }
        }

        // Manage checkbox for monitor screen off or not
        screenOffCheckBox = this.findViewById(R.id.checkBoxScreenOff)
        screenOffCheckBox!!.setOnCheckedChangeListener { view, isChecked ->
            KeepOnUtils.setResetOnScreenOff(isChecked, this)

            if (KeepOnUtils.isMyScreenOffReceiverServiceRunning() && !isChecked) {
                KeepOnUtils.stopScreenOffReceiverService(this)
            }

            if (!KeepOnUtils.isMyScreenOffReceiverServiceRunning()
                && KeepOnUtils.getKeepOn(this)
                && isChecked
            ) {
                KeepOnUtils.startScreenOffReceiverService(this)
            }

            Snackbar.make(view, getString(R.string.settings_save), Snackbar.LENGTH_LONG).show()
        }

        // Set application version on about card
        val versionTextView = this.findViewById<TextView>(R.id.card_about_version)
        var sVersion = getString(R.string.about_card_version)
        sVersion += String.format(" %s", KeepOnUtils.getAppVersion(this))
        versionTextView.text = Html.fromHtml(sVersion, HtmlCompat.FROM_HTML_MODE_LEGACY)

        animateCardView()
    }

    override fun onResume() {
        super.onResume()

        // Check permission to write settings
        if (canWrite(this)) {
            // Show dialog if missing settings on tile click
            if (intent.extras != null) {
                if (intent.extras!!.getBoolean(KeepOnUtils.TAG_MISSING_SETTINGS, false)
                    && KeepOnUtils.getSelectedTimeout(this).size <= 1
                ) {
                    if (missingSettingsDialog == null) {
                        missingSettingsDialog = KeepOnUtils.getMissingSettingsDialog(this)
                        missingSettingsDialog!!.show()
                    } else {
                        if (!missingSettingsDialog!!.isShowing) {
                            missingSettingsDialog!!.show()
                        }
                    }
                }
            }
            // Show Dialog to disable notifications if enabled
            if (KeepOnUtils.isNotificationEnabled(this)) {
                if (notificationDialog == null) {
                    notificationDialog = KeepOnUtils.getNotificationDialog(this)
                    notificationDialog!!.show()
                } else {
                    if (!notificationDialog!!.isShowing) {
                        notificationDialog!!.show()
                    }
                }
            }
            // If no custom screen timeout set update OriginalTimeout in saved preference
            if (!KeepOnUtils.getKeepOn(this))
                KeepOnUtils.updateOriginalTimeout(this)

            // Set state of screen of monitor engine checkbox from saved preference
            screenOffCheckBox!!.isChecked = KeepOnUtils.getResetOnScreenOff(this)

            // Start ScreenTimeoutObserverService if QSTile is added
            if (KeepOnUtils.getTileAdded(this) && !KeepOnUtils.isMyScreenTimeoutObserverServiceRunning())
                KeepOnUtils.startScreenTimeoutObserverService(this)

            // Request QSTile update
            val componentName = ComponentName(this.applicationContext, KeepOnTileService::class.java)
            TileService.requestListeningState(this, componentName)

            // Update all switch from saved preference
            for (timeoutSwitch in timeoutSwitchs)
                updateSwitch(timeoutSwitch.timeoutValue, timeoutSwitch.switch)
        } else {
            // Show permission Dialog
            if (permissionDialog == null) {
                permissionDialog = KeepOnUtils.getPermissionDialog(this)
                permissionDialog!!.show()
            } else {
                if (!permissionDialog!!.isShowing) {
                    permissionDialog!!.show()
                }
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
                val isDarkTheme = KeepOnUtils.getDarkTheme(this)

                KeepOnUtils.setDarkTheme(!isDarkTheme, this)
                if (isDarkTheme)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun registerBroadcastReceiver(context: Context) {
        receiver = object : BroadcastReceiver()  {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_UPDATE_UI -> {
                        // Update all switch from saved preference
                        for (timeoutSwitch in timeoutSwitchs)
                            updateSwitch(timeoutSwitch.timeoutValue, timeoutSwitch.switch)
                    }
                    ACTION_MISSING_SETTINGS -> {
                        // Show missing settings dialog
                        if (KeepOnUtils.getSelectedTimeout(context).size <= 1) {
                            if (missingSettingsDialog == null) {
                                missingSettingsDialog = KeepOnUtils.getMissingSettingsDialog(context)
                                missingSettingsDialog!!.show()
                            } else {
                                if (!missingSettingsDialog!!.isShowing) {
                                    missingSettingsDialog!!.show()
                                }
                            }
                        }
                    }
                }
            }
        }

        val intentFiler = IntentFilter()
        intentFiler.addAction(ACTION_UPDATE_UI)
        intentFiler.addAction(ACTION_MISSING_SETTINGS)
        registerReceiver(receiver, intentFiler)
    }

    private fun updateSwitch(timeout: Int, switch: Switch) {
        val selectedSwitch = KeepOnUtils.getSelectedTimeout(this)
        val originalTimeout = KeepOnUtils.getOriginalTimeout(this)
        val currentTimeout = KeepOnUtils.getCurrentTimeout(this)

        // Check for DevicePolicy restriction
        val mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        var adminTimeout = mDPM.getMaximumTimeToLock(null)
        if (adminTimeout == 0.toLong()) adminTimeout = Long.MAX_VALUE

        switch.isChecked = (selectedSwitch.contains(timeout) || originalTimeout == timeout)

        if (originalTimeout == timeout) {
            switch.isClickable = false
            switch.setTextColor(getColor(R.color.colorTextDisabled))
            switch.thumbDrawable.colorFilter =
                PorterDuffColorFilter(getColor(R.color.colorAccentVariation), PorterDuff.Mode.SRC_IN)
            switch.trackDrawable.colorFilter =
                PorterDuffColorFilter(getColor(R.color.colorAccentVariation), PorterDuff.Mode.MULTIPLY)
        } else {
            switch.isClickable = true
            switch.setTextColor(getColor(R.color.colorText))
            switch.thumbDrawable.colorFilter = null
            switch.trackDrawable.colorFilter = null
        }

        if (currentTimeout == timeout) {
            switch.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        } else {
            switch.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
        }

        if (adminTimeout < timeout) {
            switch.visibility = View.GONE
        } else {
            switch.visibility = View.VISIBLE
        }
    }

    private fun getListIntFromSwitch(): ArrayList<Int> {
        val resultList: ArrayList<Int> = ArrayList()

        for (timeoutSwitch in timeoutSwitchs) {
            if (timeoutSwitch.switch.isChecked)
                    resultList.add(timeoutSwitch.timeoutValue)
        }

        return resultList
    }

    private fun saveSelectedSwitch(view: View) {
        KeepOnUtils.setSelectedTimeout(getListIntFromSwitch(), this)
        Snackbar.make(view, getString(R.string.settings_save), Snackbar.LENGTH_LONG).show()
    }

    private fun animateCardView() {
        val cardViewContainer = findViewById<LinearLayout>(R.id.cardViewContainer)
        cardViewContainer.post {
            processCardViewAnim(selectionCard!!, 0)
            processCardViewAnim(aboutCard!!, animDuration)
        }
    }

    private fun processCardViewAnim(cardView: CardView, startDelay: Long) {
        val cx = cardView.width / 2
        val cy = cardView.height / 2

        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(cardView, cx, cy, 0f, finalRadius)

        anim.startDelay = startDelay
        anim.duration = animDuration
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

    companion object {
        const val ACTION_UPDATE_UI = "fr.twentynine.keepon.action.UPDATE_UI"
        const val ACTION_MISSING_SETTINGS = "fr.twentynine.keepon.action.MISSING_SETTINGS"

        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}
