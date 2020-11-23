package fr.twentynine.keepon.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import fr.twentynine.keepon.R
import fr.twentynine.keepon.di.ToothpickHelper
import fr.twentynine.keepon.di.annotation.ActivityScope
import fr.twentynine.keepon.utils.preferences.Preferences
import toothpick.InjectConstructor
import toothpick.ktp.delegate.lazy
import java.util.Calendar
import java.util.TimeZone

/**
 * Copyright (c) Pixplicity, Gene-rate
 * https://github.com/Pixplicity/gene-rate
 *
 * When your app has launched a couple of times, this class will ask to give your app a rating on
 * the Play Store. If the user does not want to rate your app and indicates a complaint, you have
 * the option to redirect them to a feedback link.
 *
 *
 * To use, call the following on every app start (or when appropriate):<br></br>
 * `
 * Rate mRate = new Rate.Builder(context)
 * .setTriggerCount(10)
 * .setMinimumInstallTime(TimeUnit.DAYS.toMillis(7))
 * .setMessage(R.string.my_message_text)
 * .setSnackBarParent(view)
 * .build();
 * mRate.count();
` *
 * When it is a good time to show a rating request, call:
 * `
 * mRate.showRequest();
` *
 *
 */
@ActivityScope
@InjectConstructor
class Rate(private val mActivity: AppCompatActivity) {

    private val preferences: Preferences by lazy()

    init {
        // Inject dependencies with Toothpick
        ToothpickHelper.scopedInjection(this)
    }

    private var mStoreLink: String? = null
    /**
     * Call this method whenever your app is launched to increase the launch counter. Or whenever
     * the user performs an action that indicates immersion.
     *
     * @return the [Rate] instance
     */
    fun count(): Rate {
        return increment(false)
    }

    private fun increment(force: Boolean): Rate {
        // Get current launch count
        var count = count
        // Increment, but only when we're not on a launch point. Otherwise we could miss
        // it when .count and .showRequest calls are not called exactly alternated
        val isAtLaunchPoint = remainingCount == 0L
        if (force || !isAtLaunchPoint) {
            count++
        }
        preferences.setAppLaunchCount(count)

        return this
    }

    /**
     * Returns how often The Action has been performed, ever. This is usually the app launch event.
     *
     * @return Number of times the app was launched.
     */
    private val count: Long
        get() {
            return preferences.getAppLaunchCount()
        }

    /**
     * Returns how many more times the trigger action should be performed before it triggers the
     * rating request. This can be either the first request or consequent requests after dismissing
     * previous ones. This method does NOT consider if the request will be shown at all, e.g. when
     * "don't ask again" was checked.
     *
     *
     * If this method returns `0` (zero), the next call to [.showRequest] will show the
     * dialog.
     *
     *
     * @return Remaining count before the next request is triggered.
     */
    private val remainingCount: Long
        get() {
            return if (count < DEFAULT_COUNT) {
                DEFAULT_COUNT - count
            } else {
                (DEFAULT_REPEAT_COUNT - (count - DEFAULT_COUNT) % DEFAULT_REPEAT_COUNT) % DEFAULT_REPEAT_COUNT
            }
        }

    /**
     * Checks if the app has been launched often enough to ask for a rating, and shows the rating
     * request if so. The rating request can be a SnackBar (preferred) or a dialog.
     *
     * @return If the request is shown or not
     */
    fun showRequest(): Boolean {
        val asked = preferences.getAppReviewAsked()
        val firstLaunch = mActivity.packageManager.getPackageInfo(mActivity.packageName, 0).firstInstallTime

        val shouldShowRequest = (remainingCount == 0L && !asked && Calendar.getInstance(TimeZone.getTimeZone("utc")).timeInMillis > (firstLaunch + DEFAULT_INSTALL_TIME))

        if (shouldShowRequest && canRateApp()) {
            showRatingRequest()
        }

        return shouldShowRequest
    }

    /**
     * Creates an Intent to launch the proper store page. This does not guarantee the Intent can be
     * launched (i.e. that the Play Store is installed).
     *
     * @return The Intent to launch the store.
     */
    private val storeIntent: Intent
        get() {
            val uri = Uri.parse(
                if (mStoreLink != null) {
                    mStoreLink
                } else {
                    val packageName = mActivity.packageName
                    "market://details?id=$packageName"
                }
            )
            return Intent(Intent.ACTION_VIEW, uri)
        }

    private fun showRatingRequest() {
        increment(true)
        showRatingSnackbar()
    }

    private fun showRatingSnackbar() {
        // Wie is hier nou de snackbar?
        val snackbar = Snackbar.make(mActivity.findViewById(R.id.cardViewContainer), "", Snackbar.LENGTH_INDEFINITE)

        // Set background transparent and remove padding
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        snackbar.view.setPadding(0, 0, 0, 0)

        // Get snackbar layout
        val layout = snackbar.view as SnackbarLayout

        // Hide default text
        val textView = layout.findViewById<TextView>(R.id.snackbar_text)
        textView.visibility = View.INVISIBLE

        // Inflate our custom view
        val inflater = (mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        @SuppressLint("InflateParams")
        val snackView = inflater.inflate(R.layout.generate_snackbar, null)

        // Set OnClickListener for buttons
        val btRate = snackView.findViewById<Button>(R.id.bt_positive)
        btRate.setOnClickListener {
            snackbar.dismiss()
            openPlayStore()
            saveAsked()
        }
        val btNo = snackView.findViewById<Button>(R.id.bt_negative)
        btNo.setOnClickListener {
            snackbar.dismiss()
            saveAsked()
        }

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)

        // Show the Snackbar
        snackbar.show()
    }

    private fun openPlayStore() {
        val intent = storeIntent
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        mActivity.startActivity(intent)
    }

    /**
     * Checks if the app can be rated, i.e. if the store Intent can be launched, i.e. if the Play
     * Store is installed.
     *
     * @return if the app can be rated
     * @see .getStoreIntent
     */
    private fun canRateApp(): Boolean {
        return canOpenIntent(storeIntent)
    }

    /**
     * Checks if the system or any 3rd party app can handle the Intent
     *
     * @param intent the Intent
     * @return if the Intent can be handled by the system
     */
    private fun canOpenIntent(intent: Intent): Boolean {
        return mActivity
            .packageManager
            .queryIntentActivities(intent, 0)
            .size > 0
    }

    private fun saveAsked() {
        preferences.setAppReviewAsked(true)
    }

    companion object {
        private const val DEFAULT_COUNT = 5
        private const val DEFAULT_REPEAT_COUNT = 20

        private const val DEFAULT_INSTALL_TIME = (5 * (1000 * 60 * 60 * 24)).toLong() // 5 days (1000 -> to seconds * 60 -> to minutes * 60 -> to hours * 24 -> to days)
    }
}
