package fr.twentynine.keepon.tasker

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import com.google.android.material.radiobutton.MaterialRadioButton
import fr.twentynine.keepon.R
import fr.twentynine.keepon.tasker.Intent.Companion.ACTION_EDIT_SETTING
import fr.twentynine.keepon.tasker.Intent.Companion.EXTRA_BUNDLE
import fr.twentynine.keepon.tasker.Intent.Companion.EXTRA_STRING_BLURB
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.Preferences
import kotlinx.android.synthetic.main.activity_tasker_edit.*

class EditActivity : AppCompatActivity() {
    private var isCancelled = false

    private val timeoutMap: ArrayMap<Int, Int> = arrayMapOf(
        R.id.timeout_previous to -43,
        R.id.timeout_default to -42,
        R.id.timeout_15_seconds to Preferences.getTimeoutValueArray()[0],
        R.id.timeout_30_seconds to Preferences.getTimeoutValueArray()[1],
        R.id.timeout_1_minute to Preferences.getTimeoutValueArray()[2],
        R.id.timeout_2_minutes to Preferences.getTimeoutValueArray()[3],
        R.id.timeout_5_minutes to Preferences.getTimeoutValueArray()[4],
        R.id.timeout_10_minutes to Preferences.getTimeoutValueArray()[5],
        R.id.timeout_30_minutes to Preferences.getTimeoutValueArray()[6],
        R.id.timeout_1_hour to Preferences.getTimeoutValueArray()[7],
        R.id.timeout_infinite to Preferences.getTimeoutValueArray()[8]
    )

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A hack to prevent a private serializable classloader attack
        if (BundleScrubber.scrub(intent)) {
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
        if (Preferences.getDarkTheme(this)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        setContentView(R.layout.activity_tasker_edit)
        setSupportActionBar(toolbar)

        title = getString(R.string.app_name)
        supportActionBar?.subtitle = getString(R.string.tasker_activity_name)

        isCancelled = false

        // Set FAB action
        fab_save.setOnClickListener { finish() }

        // Disable option 'previous value' if no previous value found
        if (Preferences.getPreviousValue(this) == 0) {
            timeout_previous.isEnabled = false
        }

        val forwardedBundle = intent.getBundleExtra(EXTRA_BUNDLE)

        if (PluginBundleManager.isBundleValid(forwardedBundle) && forwardedBundle != null) {
            val forwardedTimeout = forwardedBundle.getInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE)
            if (timeoutMap.containsValue(forwardedTimeout)) {
                var timeoutCheckedId = -1
                for (timeout in timeoutMap) {
                    if (timeout.value == forwardedTimeout) {
                        timeoutCheckedId = timeout.key
                        break
                    }
                }

                if (timeoutCheckedId != -1) {
                    timeout_radiogroup.check(timeoutCheckedId)
                }
            }
        }
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
            if (!timeoutMap.containsKey(timeout_radiogroup.checkedRadioButtonId)) {
                setResult(RESULT_CANCELED)
            } else {
                val timeoutCheckedId = timeout_radiogroup.checkedRadioButtonId
                val timeout = timeoutMap[timeoutCheckedId]
                val timeoutText = findViewById<MaterialRadioButton>(timeoutCheckedId).text

                val resultIntent = Intent()
                val resultBundle = Bundle()
                resultBundle.putInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE, timeout!!)
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
