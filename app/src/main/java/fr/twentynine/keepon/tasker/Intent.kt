/*
 * android-plugin-api-for-locale https://github.com/twofortyfouram/android-plugin-api-for-locale
 * Copyright 2014 two forty four a.m. LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package fr.twentynine.keepon.tasker

/**
 * Contains Intent constants necessary for interacting with the plug-in API for Locale.
 */
class Intent private constructor() {
    companion object {
        /**
         *
         * `Intent` action sent by the host to create or
         * edit a plug-in condition. When the host sends this `Intent`, it
         * will be explicit (i.e. sent directly to the package and class of the plug-in's
         * `Activity`).
         *
         * The `Intent` MAY contain
         * [.EXTRA_BUNDLE] and [.EXTRA_STRING_BLURB] that was previously set by the `Activity` result of ACTION_EDIT_CONDITION.
         *
         * There SHOULD be only one `Activity` per APK that implements this
         * `Intent`. If a single APK wishes to export multiple plug-ins, it
         * MAY implement multiple Activity instances that implement this
         * `Intent`, however there must only be a single
         * [.ACTION_QUERY_CONDITION] receiver. In such a scenario, it is the
         * responsibility of the Activity to store enough data in
         * [.EXTRA_BUNDLE] to allow this receiver to disambiguate which
         * "plug-in" is being queried. To avoid user confusion, it is recommended
         * that only a single plug-in be implemented per APK.
         *
         * @see Intent.EXTRA_BUNDLE
         *
         * @see Intent.EXTRA_STRING_BREADCRUMB
         */
        const val ACTION_EDIT_CONDITION =
            "com.twofortyfouram.locale.intent.action.EDIT_CONDITION"

        /**
         *
         * Ordered `Intent` action broadcast by the host to query
         * a plug-in condition. When the host broadcasts this `Intent`, it will
         * be explicit (i.e. directly to the package and class of the plug-in's
         * `BroadcastReceiver`).
         *
         * The `Intent` MUST contain a
         * [.EXTRA_BUNDLE] that was previously set by the `Activity`
         * result of [.ACTION_EDIT_CONDITION].
         *
         *
         *
         * Since this is an ordered broadcast, the plug-in's receiver MUST set an
         * appropriate result code from [.RESULT_CONDITION_SATISFIED],
         * [.RESULT_CONDITION_UNSATISFIED], or
         * [.RESULT_CONDITION_UNKNOWN].
         *
         *
         * There MUST be only one `BroadcastReceiver` per APK that implements
         * an Intent-filter for this action.
         *
         *
         * @see Intent.EXTRA_BUNDLE
         *
         * @see Intent.RESULT_CONDITION_SATISFIED
         *
         * @see Intent.RESULT_CONDITION_UNSATISFIED
         *
         * @see Intent.RESULT_CONDITION_UNKNOWN
         */
        const val ACTION_QUERY_CONDITION =
            "com.twofortyfouram.locale.intent.action.QUERY_CONDITION"

        /**
         *
         *
         * `Intent` action sent by the host to create or
         * edit a plug-in setting. When the host sends this `Intent`, it
         * will be sent explicit (i.e. sent directly to the package and class of the plug-in's
         * `Activity`).
         *
         * The `Intent` MAY contain a [.EXTRA_BUNDLE] and [ ][.EXTRA_STRING_BLURB]
         * that was previously set by the `Activity` result of
         * ACTION_EDIT_SETTING.
         *
         *
         * There SHOULD be only one `Activity` per APK that implements this
         * `Intent`. If a single APK wishes to export multiple plug-ins, it
         * MAY implement multiple Activity instances that implement this
         * `Intent`, however there must only be a single
         * [.ACTION_FIRE_SETTING] receiver. In such a scenario, it is the
         * responsibility of the Activity to store enough data in
         * [.EXTRA_BUNDLE] to allow this receiver to disambiguate which
         * "plug-in" is being fired. To avoid user confusion, it is recommended that
         * only a single plug-in be implemented per APK.
         *
         *
         * @see Intent.EXTRA_BUNDLE
         *
         * @see Intent.EXTRA_STRING_BREADCRUMB
         */
        const val ACTION_EDIT_SETTING =
            "com.twofortyfouram.locale.intent.action.EDIT_SETTING"

        /**
         *
         *
         * `Intent` action broadcast by the host to fire a
         * plug-in setting. When the host broadcasts this `Intent`, it will be
         * explicit (i.e. sent directly to the package and class of the plug-in's
         * `BroadcastReceiver`).
         *
         * The `Intent` MUST contain a
         * [.EXTRA_BUNDLE] that was previously set by the `Activity`
         * result of [.ACTION_EDIT_SETTING].
         *
         *
         * There MUST be only one `BroadcastReceiver` per APK that implements
         * an Intent-filter for this action.
         *
         * @see Intent.EXTRA_BUNDLE
         */
        const val ACTION_FIRE_SETTING =
            "com.twofortyfouram.locale.intent.action.FIRE_SETTING"

        /**
         *
         * Implicit broadcast `Intent` action to notify the host(s) that a plug-in
         * condition is requesting a query it via
         * [.ACTION_QUERY_CONDITION]. This merely serves as a hint to the host
         * that a condition wants to be queried. There is no guarantee as to when or
         * if the plug-in will be queried after this action is broadcast. If
         * the host does not respond to the plug-in condition after a
         * ACTION_REQUEST_QUERY Intent is sent, the plug-in SHOULD shut
         * itself down and stop requesting requeries. A lack of response from the host
         * indicates that the host is not currently interested in this plug-in. When
         * the host becomes interested in the plug-in again, the host will send
         * [.ACTION_QUERY_CONDITION].
         *
         *
         * The extra [.EXTRA_STRING_ACTIVITY_CLASS_NAME] MUST be included, otherwise the host will
         * ignore this `Intent`.
         *
         *
         *
         * Plug-in conditions SHOULD NOT use this unless there is some sort of
         * asynchronous event that has occurred, such as a broadcast `Intent`
         * being received by the plug-in. Plug-ins SHOULD NOT periodically request a
         * requery as a way of implementing polling behavior.
         *
         *
         *
         * Hosts MAY throttle plug-ins that request queries too frequently.
         *
         *
         * @see Intent.EXTRA_STRING_ACTIVITY_CLASS_NAME
         */
        const val ACTION_REQUEST_QUERY =
            "com.twofortyfouram.locale.intent.action.REQUEST_QUERY"

        /**
         *
         *
         * Type: `String`.
         *
         *
         *
         * Maps to a `String` that represents the `Activity` bread crumb
         * path.
         *
         */
        const val EXTRA_STRING_BREADCRUMB =
            "com.twofortyfouram.locale.intent.extra.BREADCRUMB"

        /**
         *
         *
         * Type: `String`.
         *
         *
         *
         * Maps to a `String` that represents a blurb. This is returned as an
         * `Activity` result extra from the Activity started with [.ACTION_EDIT_CONDITION]
         * or
         * [.ACTION_EDIT_SETTING].
         *
         *
         *
         * The blurb is a concise description displayed to the user of what the
         * plug-in is configured to do.
         *
         */
        const val EXTRA_STRING_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB"

        /**
         *
         *
         * Type: `Bundle`.
         *
         *
         * Maps to a `Bundle` that contains all of a plug-in's extras to later be used when
         * querying or firing the plug-in.
         *
         *
         *
         * Plug-ins MUST NOT store Parcelable objects in this `Bundle`
         * , because `Parcelable` is not a long-term storage format.
         *
         *
         * Plug-ins MUST NOT store any serializable object that is not exposed by
         * the Android SDK.  Plug-ins SHOULD NOT store any serializable object that is not available
         * across all Android API levels that the plug-in supports.  Doing could cause previously saved
         * plug-ins to fail during backup and restore.
         *
         *
         *
         * When the Bundle is serialized by the host, the maximum size of the serialized Bundle MUST be
         * less than 25 kilobytes (base-10).  While the serialization mechanism used by the host is
         * opaque to the plug-in, in general plug-ins should just make their Bundle reasonably compact.
         * In Android, Intent extras are limited to about 500 kilobytes, although the exact
         * size is not specified by the Android public API.  If an Intent exceeds that size, the extras
         * will be silently dropped by Android. In Android 4.4 KitKat, the maximum amount of data that
         * can be written to a ContentProvider during a ContentProviderOperation was reduced to
         * less than 300 kilobytes. The maximum bundle size here was chosen to allow several large
         * plug-ins to be added to a single batch of operations before overflow occurs.
         *
         *
         * If a plug-in needs to store large amounts of data, the plug-in should consider
         * implementing its own internal storage mechanism.  The Bundle can then contain a small token
         * that the plug-in uses as a lookup key in its own internal storage mechanism.
         */
        const val EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE"

        /**
         *
         *
         * Type: `String`.
         *
         *
         *
         * Maps to a `String` that is the fully qualified class name of a plug-in's
         * `Activity`.
         *
         *
         * @see Intent.ACTION_REQUEST_QUERY
         */
        const val EXTRA_STRING_ACTIVITY_CLASS_NAME =
            "com.twofortyfouram.locale.intent.extra.ACTIVITY"

        /**
         *
         * Ordered broadcast result code indicating that a plug-in condition's state
         * is satisfied (true).
         *
         * @see Intent.ACTION_QUERY_CONDITION
         */
        const val RESULT_CONDITION_SATISFIED = 16

        /**
         * Ordered broadcast result code indicating that a plug-in condition's state
         * is not satisfied (false).
         *
         * @see Intent.ACTION_QUERY_CONDITION
         */
        const val RESULT_CONDITION_UNSATISFIED = 17

        /**
         *
         *
         * Ordered broadcast result code indicating that a plug-in condition's state
         * is unknown (neither true nor false).
         *
         *
         *
         * If a condition returns UNKNOWN, then the host will use the last known
         * return value on a best-effort basis. Best-effort means that the host may
         * not persist known values forever (e.g. last known values could
         * hypothetically be cleared after a device reboot or a restart of the
         * host's process. If there is no last known return value, then unknown is
         * treated as not satisfied (false).
         *
         *
         *
         * The purpose of an UNKNOWN result is to allow a plug-in condition more
         * than 10 seconds to process a query. A `BroadcastReceiver` MUST
         * return within 10 seconds, otherwise it will be killed by Android. A
         * plug-in that needs more than 10 seconds might initially return
         * RESULT_CONDITION_UNKNOWN, subsequently request a requery, and
         * then return either [.RESULT_CONDITION_SATISFIED] or
         * [.RESULT_CONDITION_UNSATISFIED].
         *
         *
         * @see Intent.ACTION_QUERY_CONDITION
         */
        const val RESULT_CONDITION_UNKNOWN = 18
    }

    /**
     * Private constructor prevents instantiation.
     *
     * @throws - UnsupportedOperationException because this class cannot be
     * instantiated.
     */
    init {
        throw UnsupportedOperationException("This class is non-instantiable")
    }
}
