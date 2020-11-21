package fr.twentynine.keepon.tasker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import com.google.android.material.radiobutton.MaterialRadioButton
import fr.twentynine.keepon.KeepOnApplication.Companion.viewBinding
import fr.twentynine.keepon.R
import fr.twentynine.keepon.databinding.ActivityTaskerEditBinding
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.tasker.Intent.Companion.ACTION_EDIT_SETTING
import fr.twentynine.keepon.tasker.Intent.Companion.EXTRA_BUNDLE
import fr.twentynine.keepon.tasker.Intent.Companion.EXTRA_STRING_BLURB
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.ktp.delegate.lazy

class EditActivity : AppCompatActivity() {

    private val bundleScrubber: BundleScrubber by lazy()
    private val preferences: Preferences by lazy()

    private val binding: ActivityTaskerEditBinding by viewBinding(ActivityTaskerEditBinding::inflate)

    private val timeoutMap: ArrayMap<Int, Int> by lazy {
        arrayMapOf(
            R.id.timeout_previous to -43,
            R.id.timeout_default to -42,
            R.id.timeout_15_seconds to preferences.getTimeoutValueArray()[0],
            R.id.timeout_30_seconds to preferences.getTimeoutValueArray()[1],
            R.id.timeout_1_minute to preferences.getTimeoutValueArray()[2],
            R.id.timeout_2_minutes to preferences.getTimeoutValueArray()[3],
            R.id.timeout_5_minutes to preferences.getTimeoutValueArray()[4],
            R.id.timeout_10_minutes to preferences.getTimeoutValueArray()[5],
            R.id.timeout_30_minutes to preferences.getTimeoutValueArray()[6],
            R.id.timeout_1_hour to preferences.getTimeoutValueArray()[7],
            R.id.timeout_infinite to preferences.getTimeoutValueArray()[8]
        )
    }

    private var isCancelled = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)

        // A hack to prevent a private serializable classloader attack
        if (bundleScrubber.scrub(intent)) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Check that the Intent action will be ACTION_EDIT_SETTING
        if (ACTION_EDIT_SETTING != intent.action) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Ignore implicit intents, because they are not valid.
        if (packageName != intent.getPackage() && ComponentName(this, this.javaClass.name) != intent.component) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        // Set DarkTheme
        if (preferences.getDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        setSupportActionBar(binding.toolbar)

        title = getString(R.string.app_name)
        supportActionBar?.subtitle = getString(R.string.tasker_activity_name)

        isCancelled = false

        // Set FAB action
        binding.fabSave.setOnClickListener { finish() }

        // Disable option 'previous value' if no previous value found
        if (preferences.getPreviousValue() == 0) {
            binding.timeoutPrevious.isEnabled = false
        }

        // Check for DevicePolicy restriction
        val mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        var adminTimeout = mDPM.getMaximumTimeToLock(null)
        if (adminTimeout == 0L) adminTimeout = Long.MAX_VALUE

        for (timeout in timeoutMap) {
            if (adminTimeout < timeout.value) {
                findViewById<MaterialRadioButton>(timeout.key).visibility = View.GONE
            } else {
                findViewById<MaterialRadioButton>(timeout.key).visibility = View.VISIBLE
            }
        }

        val forwardedBundle = intent.getBundleExtra(EXTRA_BUNDLE)

        if (forwardedBundle != null && PluginBundleManager.isBundleValid(forwardedBundle)) {
            val forwardedTimeout = forwardedBundle.getInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE)
            if (timeoutMap.containsValue(forwardedTimeout)) {
                var timeoutCheckedId = -1
                for (timeout in timeoutMap) {
                    if (timeout.value == forwardedTimeout && timeout.value <= adminTimeout) {
                        timeoutCheckedId = timeout.key
                        break
                    }
                }

                if (timeoutCheckedId != -1) {
                    binding.timeoutRadiogroup.check(timeoutCheckedId)
                }
            }
        }

        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tasker_action_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_dontsave -> {
                isCancelled = true
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        isCancelled = true
        finish()
    }

    override fun finish() {
        if (isCancelled) {
            setResult(RESULT_CANCELED)
        } else {
            if (!timeoutMap.containsKey(binding.timeoutRadiogroup.checkedRadioButtonId)) {
                setResult(RESULT_CANCELED)
            } else {
                val timeoutCheckedId = binding.timeoutRadiogroup.checkedRadioButtonId
                val timeout = timeoutMap[timeoutCheckedId]
                val timeoutText = findViewById<MaterialRadioButton>(timeoutCheckedId).text

                val resultIntent = Intent()
                val resultBundle = Bundle()
                if (timeout != null) resultBundle.putInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE, timeout)
                resultIntent.putExtra(EXTRA_BUNDLE, resultBundle)

                // add text for display in tasker
                resultIntent.putExtra(EXTRA_STRING_BLURB, timeoutText)

                setResult(RESULT_OK, resultIntent)
            }
        }
        timeoutMap.clear()
        super.finish()
    }
}
