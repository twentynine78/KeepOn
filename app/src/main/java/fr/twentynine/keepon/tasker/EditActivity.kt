package fr.twentynine.keepon.tasker

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.radiobutton.MaterialRadioButton
import fr.twentynine.keepon.R
import fr.twentynine.keepon.utils.BundleScrubber
import fr.twentynine.keepon.utils.KeepOnUtils
import fr.twentynine.keepon.tasker.Intent.Companion.ACTION_EDIT_SETTING
import fr.twentynine.keepon.tasker.Intent.Companion.EXTRA_BUNDLE
import fr.twentynine.keepon.tasker.Intent.Companion.EXTRA_STRING_BLURB
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.HashMap


class EditActivity : AppCompatActivity() {
    private var isCancelled = false
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
        if (KeepOnUtils.getDarkTheme(this))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.activity_tasker_edit)
        setSupportActionBar(toolbar)

        title = getString(R.string.app_name)
        supportActionBar?.subtitle = getString(R.string.tasker_activity_name)

        isCancelled = false

        // Set FAB action
        val fab = findViewById<FloatingActionButton>(R.id.fab_save)
        fab.setOnClickListener { finish() }

        // Disable option 'previous value' if no previous value found
        if (KeepOnUtils.getPreviousTimeout(this) == 0)
            findViewById<MaterialRadioButton>(R.id.timeout_previous).isEnabled = false

        val forwardedBundle = intent.getBundleExtra(EXTRA_BUNDLE)

        if (PluginBundleManager.isBundleValid(forwardedBundle) && forwardedBundle != null) {
            if (timeoutMap.filterValues { it == forwardedBundle.getInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE) }.keys.isNotEmpty()) {
                (findViewById<View>(R.id.timeout_radiogroup) as RadioGroup).check(
                    timeoutMap.filterValues { it == forwardedBundle.getInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE) }.keys.first()
                )
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
            val timeout = timeoutMap[(findViewById<View>(R.id.timeout_radiogroup) as RadioGroup).checkedRadioButtonId]

            if (timeoutMap.filterValues { it == timeout }.keys.isNullOrEmpty()) {
                setResult(RESULT_CANCELED)
            } else {
                val resultIntent = Intent()
                val resultBundle = Bundle()
                resultBundle.putInt(PluginBundleManager.BUNDLE_EXTRA_TIMEOUT_VALUE, timeout!!)
                resultIntent.putExtra(EXTRA_BUNDLE, resultBundle)

                val timeoutText = findViewById<MaterialRadioButton>(timeoutMap.filterValues { it == timeout }.keys.first()).text

                // add text for display in tasker
                resultIntent.putExtra(EXTRA_STRING_BLURB, timeoutText)

                setResult(RESULT_OK, resultIntent)
            }
        }
        super.finish()
    }

    companion object {
        private val timeoutMap: HashMap<Int, Int> = hashMapOf(
            R.id.timeout_previous to -43,
            R.id.timeout_default to -42,
            R.id.timeout_15_seconds to KeepOnUtils.getTimeoutValueArray()[0],
            R.id.timeout_30_seconds to KeepOnUtils.getTimeoutValueArray()[1],
            R.id.timeout_1_minute to KeepOnUtils.getTimeoutValueArray()[2],
            R.id.timeout_2_minutes to KeepOnUtils.getTimeoutValueArray()[3],
            R.id.timeout_5_minutes to KeepOnUtils.getTimeoutValueArray()[4],
            R.id.timeout_10_minutes to KeepOnUtils.getTimeoutValueArray()[5],
            R.id.timeout_30_minutes to KeepOnUtils.getTimeoutValueArray()[6],
            R.id.timeout_1_hour to KeepOnUtils.getTimeoutValueArray()[7],
            R.id.timeout_infinite to KeepOnUtils.getTimeoutValueArray()[8]
        )
    }
}