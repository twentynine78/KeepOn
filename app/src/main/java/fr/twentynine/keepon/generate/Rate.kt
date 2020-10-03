package fr.twentynine.keepon.generate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import fr.twentynine.keepon.R
import fr.twentynine.keepon.utils.preferences.Preferences

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
class Rate private constructor(private val mContext: Context) {
    private val mPackageName: String = mContext.packageName
    private val mMessage = mContext.getString(R.string.generate_please_rate)
    private val mTextNever = mContext.getString(R.string.generate_button_dont_ask)
    private val mTextFeedback = mContext.getString(R.string.generate_button_feedback)
    private val mTriggerCount = DEFAULT_COUNT
    private val mMinInstallTime = DEFAULT_INSTALL_TIME
    private val mRepeatCount = DEFAULT_REPEAT_COUNT
    private var mParentView: ViewGroup? = null
    private var mFeedbackAction: OnFeedbackListener? = null
    private var mSnackBarSwipeToDismiss = true
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
        Preferences.setGenerateLaunchCount(count, mContext)

        return this
    }

    /**
     * Returns how often The Action has been performed, ever. This is usually the app launch event.
     *
     * @return Number of times the app was launched.
     */
    private val count: Long
        get() {
            return Preferences.getGenerateLaunchCount(mContext)
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
            return if (count < mTriggerCount) {
                mTriggerCount - count
            } else {
                (mRepeatCount - (count - mTriggerCount) % mRepeatCount) % mRepeatCount
            }
        }

    /**
     * Checks if the app has been launched often enough to ask for a rating, and shows the rating
     * request if so. The rating request can be a SnackBar (preferred) or a dialog.
     *
     * @return If the request is shown or not
     * @see Builder.setSnackBarParent
     */
    fun showRequest(): Boolean {
        val asked = Preferences.getGenerateBoolAsked(mContext)
        val firstLaunch = mContext.packageManager.getPackageInfo(mContext.packageName, 0).firstInstallTime

        val shouldShowRequest = (remainingCount == 0L && !asked && System.currentTimeMillis() > (firstLaunch + mMinInstallTime))

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
                if (mStoreLink != null)
                    mStoreLink
                else
                    "market://details?id=$mPackageName"
            )
            return Intent(Intent.ACTION_VIEW, uri)
        }

    private fun showRatingRequest() {
        increment(true)
        showRatingSnackbar()
    }

    private fun showRatingSnackbar() {
        // Wie is hier nou de snackbar?
        val snackbar = Snackbar.make(
            mParentView!!, mMessage,
            if (mSnackBarSwipeToDismiss)
                Snackbar.LENGTH_INDEFINITE
            else
                Snackbar.LENGTH_LONG
        )
        val layout = snackbar.view as SnackbarLayout

        // Hide default text
        val textView = layout.findViewById<TextView>(R.id.snackbar_text)
        textView.visibility = View.INVISIBLE

        // Inflate our custom view
        val inflater = (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        val layoutId: Int = R.layout.generate_snackbar
        val snackView = inflater.inflate(layoutId, null)

        // Configure the view
        val tvMessage = snackView.findViewById<TextView>(R.id.text)
        tvMessage.text = mMessage

        val cbNever = snackView.findViewById<CheckBox>(R.id.cb_never)
        cbNever.text = mTextNever
        cbNever.isChecked = DEFAULT_CHECKED

        val btFeedback = snackView.findViewById<Button>(R.id.bt_negative)
        btFeedback.text = mTextFeedback
        btFeedback.paintFlags = btFeedback.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val btRate = snackView.findViewById<Button>(R.id.bt_positive)
        snackView.findViewById<View>(R.id.tv_swipe).visibility =
            if (mSnackBarSwipeToDismiss)
                View.VISIBLE
            else
                View.GONE

        // Remember to not ask again if user swiped it
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar, @DismissEvent event: Int) {
                super.onDismissed(transientBottomBar, event)
                if (event == DISMISS_EVENT_SWIPE && cbNever.isChecked) {
                    saveAsked()
                }
                if (mFeedbackAction != null) {
                    mFeedbackAction!!.onRequestDismissed(cbNever.isChecked)
                }
            }
        })

        // Rate listener
        btRate.setOnClickListener {
            snackbar.dismiss()
            openPlayStore()
            saveAsked()
            if (mFeedbackAction != null) {
                mFeedbackAction!!.onRateTapped()
            }
        }

        // Feedback listener
        if (mFeedbackAction != null) {
            btFeedback.text = mTextFeedback
            btFeedback.visibility = View.VISIBLE
            btFeedback.setOnClickListener {
                if (cbNever.isChecked) {
                    saveAsked()
                }
                snackbar.dismiss()
                if (mFeedbackAction != null) {
                    mFeedbackAction!!.onFeedbackTapped()
                }
            }
        }

        // Checkbox listener
        cbNever.setOnCheckedChangeListener { _, checked ->
            Preferences.setGenerateBoolAsked(checked, mContext)
        }

        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)

        // Show the Snackbar
        snackbar.show()
    }

    private fun openPlayStore() {
        val intent = storeIntent
        if (mContext !is Activity) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        mContext.startActivity(intent)
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
        return mContext
            .packageManager
            .queryIntentActivities(intent, 0)
            .size > 0
    }

    private fun saveAsked() {
        Preferences.setGenerateBoolAsked(true, mContext)
    }

    class Builder(context: Context) {
        private val mRate: Rate = Rate(context)

        /**
         * Sets the Uri to open when the user clicks the feedback button.
         * This can use the scheme `mailto:`, `tel:`, `geo:`, `https:`, etc.
         *
         * @param uri The Uri to open, or `null` to hide the feedback button
         * @return The current [Builder]
         * @see .setFeedbackAction
         */
        fun setFeedbackAction(uri: Uri?): Builder {
            if (uri == null) {
                mRate.mFeedbackAction = null
            } else {
                mRate.mFeedbackAction = object : OnFeedbackAdapter() {
                    override fun onFeedbackTapped() {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        if (mRate.canOpenIntent(intent)) {
                            mRate.mContext.startActivity(intent)
                        }
                    }
                }
            }
            return this
        }

        /**
         * Sets the parent view for a Snackbar. This enables the use of a Snackbar for the rating
         * request instead of the default dialog.
         *
         * @param parent The parent view to put the Snackbar in, or `null` to disable the
         * Snackbar
         * @return The current [Builder]
         */
        fun setSnackBarParent(parent: ViewGroup?): Builder {
            mRate.mParentView = parent
            return this
        }

        /**
         * Shows or hides the 'swipe to dismiss' notion in the Snackbar. When disabled, the
         * Snackbar will automatically hide after a view seconds. When enabled, the Snackbar will
         * show indefinitely until dismissed by the user. **Note that the
         * Snackbar can only be swiped when one of the parent views is a
         * `CoordinatorLayout`!** Also, **toggling this does not change
         * if the Snackbar can actually be swiped to dismiss!**
         *
         * @param visible Show/hide the 'swipe to dismiss' text, and disable/enable auto-hide.
         * Default is {code true}.
         * @return The current [Builder]
         */
        fun setSwipeToDismissVisible(visible: Boolean): Builder {
            mRate.mSnackBarSwipeToDismiss = visible
            return this
        }

        /**
         * Build the [Rate] instance
         *
         * @return a new Rate instance as configured by the current [Builder]
         */
        fun build(): Rate {
            return mRate
        }

        /* Unused functions

    private var mTextPositive = mContext.getString(R.string.generate_button_yes)
    private var mTextNegative = mContext.getString(R.string.generate_button_feedback)
    private var mTextCancel = mContext.getString(R.string.generate_button_no)

        /**
         * Set number of times [.count] should be called before triggering the rating
         * request
         *
         * @param count Number of times (inclusive) to call [.count] before rating
         * request should show. Defaults to [.DEFAULT_COUNT]
         * @return The current [Builder]
         */
        fun setTriggerCount(count: Int): Builder {
            mRate.mTriggerCount = count
            return this
        }

        /**
         * Set amount of time the app should be installed before asking for a rating. Defaults to 5
         * days.
         *
         * @param millis Amount of time in milliseconds the app should be installed before asking a
         * rating.
         * @return The current [Builder]
         */
        fun setMinimumInstallTime(millis: Int): Builder {
            mRate.mMinInstallTime = millis.toLong()
            return this
        }

        /**
         * Sets the repeat count to bother the user again if "don't ask again" was checked.
         *
         * @param repeatCount Integer how often rate will wait if "don't ask again" was checked
         * (default 30).
         * @return The current [Builder]
         */
        fun setRepeatCount(repeatCount: Int): Builder {
            mRate.mRepeatCount = repeatCount
            return this
        }

        /**
         * Sets the message to show in the rating request.
         *
         * @param resId The message that asks the user for a rating
         * @return The current [Builder]
         * @see .setMessage
         */
        fun setMessage(@StringRes resId: Int): Builder {
            return setMessage(mRate.mContext.getString(resId))
        }

        /**
         * Sets the text to show in the rating request on the positive button.
         *
         * @param message The text on the positive button
         * @return The current [Builder]
         * @see .setPositiveButton
         */
        private fun setPositiveButton(message: CharSequence?): Builder {
            mRate.mTextPositive = message.toString()
            return this
        }

        /**
         * Sets the text to show in the rating request on the positive button.
         *
         * @param resId The text on the positive button
         * @return The current [Builder]
         * @see .setPositiveButton
         */
        fun setPositiveButton(@StringRes resId: Int): Builder {
            return setPositiveButton(mRate.mContext.getString(resId))
        }

        /**
         * Sets the message to show in the rating request.
         *
         * @param message The message that asks the user for a rating
         * @return The current [Builder]
         * @see .setMessage
         */
        private fun setMessage(message: CharSequence?): Builder {
            mRate.mMessage = message.toString()
            return this
        }

        /**
         * Sets the text to show in the rating request on the negative button.
         *
         * @param message The text on the negative button
         * @return The current [Builder]
         * @see .setNegativeButton
         */
        private fun setNegativeButton(message: CharSequence?): Builder {
            mRate.mTextNegative = message.toString()
            return this
        }

        /**
         * Sets the text to show in the rating request on the negative button.
         *
         * @param resId The text on the negative button
         * @return The current [Builder]
         * @see .setNegativeButton
         */
        fun setNegativeButton(@StringRes resId: Int): Builder {
            return setNegativeButton(mRate.mContext.getString(resId))
        }

        /**
         * Sets the text to show in the rating request on the cancel button.
         * Note that this will not be used when using a SnackBar.
         *
         * @param message The text on the cancel button
         * @return The current [Builder]
         * @see .setSnackBarParent
         * @see .setCancelButton
         */
        fun setCancelButton(message: CharSequence?): Builder {
            mRate.mTextCancel = message.toString()
            return this
        }

        /**
         * Sets the text to show in the rating request on the cancel button.
         * Note that this will not be used when using a SnackBar.
         *
         * @param resId The text on the cancel button
         * @return The current [Builder]
         * @see .setSnackBarParent
         * @see .setCancelButton
         */
        fun setCancelButton(@StringRes resId: Int): Builder {
            return setCancelButton(mRate.mContext.getString(resId))
        }

        /**
         * Sets the text to show in the rating request on the checkbox.
         *
         * @param message The text on the checkbox
         * @return The current [Builder]
         */
        fun setNeverAgainText(message: CharSequence?): Builder {
            mRate.mTextNever = message.toString()
            return this
        }

        /**
         * Sets the text to show in the rating request on the checkbox.
         *
         * @param resId The text on the checkbox
         * @return The current [Builder]
         */
        fun setNeverAgainText(@StringRes resId: Int): Builder {
            return setNeverAgainText(mRate.mContext.getString(resId))
        }

        /**
         * Sets the text to show in the feedback link.
         *
         * @param message The text in the link
         * @return The current [Builder]
         */
        fun setFeedbackText(message: CharSequence?): Builder {
            mRate.mTextFeedback = message.toString()
            return this
        }

        /**
         * Sets the text to show in the feedback link.
         *
         * @param resId The text in the link
         * @return The current [Builder]
         */
        fun setFeedbackText(@StringRes resId: Int): Builder {
            return setFeedbackText(mRate.mContext.getString(resId))
        }

        /**
         * Sets the action to perform when the user clicks the feedback button.
         *
         * @param action Callback when the user taps the feedback button, or `null` to hide
         * the feedback button
         * @return The current [Builder]
         * @see .setFeedbackAction
         */
        fun setFeedbackAction(action: OnFeedbackListener?): Builder {
            mRate.mFeedbackAction = action
            return this
        }

        /**
         * Sets the destination rate link if not Google Play.
         *
         * @param rateDestinationStore The destination link
         * (i.e. "amzn://apps/android?p=com.pixplicity.generate" ).
         * Keeps default Google Play store
         * as destination if rateDestinationStore is `null` or
         * empty.
         * @return The current [Builder]
         */
        fun setRateDestinationStore(rateDestinationStore: String?): Builder {
            if (!TextUtils.isEmpty(rateDestinationStore)) {
                mRate.mStoreLink = rateDestinationStore
            }
            return this
        }
        */
    }

    companion object {
        private const val DEFAULT_COUNT = 5
        private const val DEFAULT_REPEAT_COUNT = 15

        private const val DEFAULT_INSTALL_TIME = (5 * (1000 * 60 * 60 * 24)).toLong() // 5 days (1000 -> to seconds * 60 -> to minutes * 60 -> to hours * 24 -> to days)
        private const val DEFAULT_CHECKED = true
    }
}
