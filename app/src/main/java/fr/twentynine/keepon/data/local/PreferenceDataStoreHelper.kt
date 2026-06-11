package fr.twentynine.keepon.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.twentynine.keepon.data.enums.DataStoreSourceType
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper.Companion.USER_PREFERENCES_BACKED_UP_NAME
import fr.twentynine.keepon.data.local.PreferenceDataStoreHelper.Companion.USER_PREFERENCES_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.dataStoreBackedUp by preferencesDataStore(
    name = USER_PREFERENCES_BACKED_UP_NAME,
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = USER_PREFERENCES_BACKED_UP_NAME,
                keysToMigrate = setOf(
                    "selectedTimeout",
                    "resetTimeoutOnScreenOff",
                    "timeoutIconStyle",
                    "appLaunchCount",
                    "skipIntro",
                    "appReviewAsked",
                ),
            )
        )
    },
)
private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = USER_PREFERENCES_NAME,
                keysToMigrate = setOf(
                    "originalTimeout",
                    "newValue",
                    "previousValue",
                    "tileAdded",
                )
            )
        )
    },
)

/**
 * Thin typed wrapper over the two Preferences DataStores ([DataStoreSourceType] picks which), giving
 * the repositories read/write/remove access without touching DataStore directly. `getPreference`
 * returns a reactive [Flow] that re-emits on every change; `getLastPreference` reads the current
 * value once. IO errors reading the store are swallowed and surface as the default / empty value.
 */
interface PreferenceDataStoreHelper {
    suspend fun <T> putPreference(key: Preferences.Key<T>, value: T, dataStoreSourceType: DataStoreSourceType)

    /** Atomic read-modify-write of one key: [transform] receives the current value (null when unset). */
    suspend fun <T> updatePreference(
        key: Preferences.Key<T>,
        dataStoreSourceType: DataStoreSourceType,
        transform: (T?) -> T
    )

    suspend fun <T> removePreference(key: Preferences.Key<T>, dataStoreSourceType: DataStoreSourceType)
    fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T, dataStoreSourceType: DataStoreSourceType): Flow<T>
    fun <T> getPreference(key: Preferences.Key<T>, dataStoreSourceType: DataStoreSourceType): Flow<T?>
    /** One-shot read of the current value, or [defaultValue] when the key is unset. */
    suspend fun <T> getLastPreference(
        key: Preferences.Key<T>,
        defaultValue: T,
        dataStoreSourceType: DataStoreSourceType
    ): T
    suspend fun <T> getLastPreference(key: Preferences.Key<T>, dataStoreSourceType: DataStoreSourceType): T?

    companion object {
        const val USER_PREFERENCES_BACKED_UP_NAME = "keepon_prefs_backup"
        const val USER_PREFERENCES_NAME = "keepon_prefs"
    }
}

/**
 * DataStore-backed [PreferenceDataStoreHelper]. Owns the two `preferencesDataStore` delegates and
 * their one-time SharedPreferences migrations (carrying the original app's keys into DataStore).
 */
class PreferenceDataStoreHelperImpl @Inject constructor(@param:ApplicationContext private val context: Context) : PreferenceDataStoreHelper {

    private fun getDataSource(dataStoreSourceType: DataStoreSourceType): DataStore<Preferences> {
        return when (dataStoreSourceType) {
            DataStoreSourceType.DATA_SOURCE_BACKED_UP -> context.dataStoreBackedUp
            DataStoreSourceType.DATA_SOURCE -> context.dataStore
        }
    }

    override fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T, dataStoreSourceType: DataStoreSourceType):
        Flow<T> = getDataSource(dataStoreSourceType).data
        .orEmptyOnIoError()
        .map { preferences -> preferences[key] ?: defaultValue }

    override fun <T> getPreference(key: Preferences.Key<T>, dataStoreSourceType: DataStoreSourceType):
        Flow<T?> = getDataSource(dataStoreSourceType).data
        .orEmptyOnIoError()
        .map { preferences -> preferences[key] }

    override suspend fun <T> getLastPreference(
        key: Preferences.Key<T>,
        defaultValue: T,
        dataStoreSourceType: DataStoreSourceType
    ): T = getDataSource(dataStoreSourceType).data.orEmptyOnIoError().firstOrNull()?.get(key) ?: defaultValue

    override suspend fun <T> getLastPreference(
        key: Preferences.Key<T>,
        dataStoreSourceType: DataStoreSourceType
    ): T? = getDataSource(dataStoreSourceType).data.orEmptyOnIoError().firstOrNull()?.get(key)

    override suspend fun <T> putPreference(
        key: Preferences.Key<T>,
        value: T,
        dataStoreSourceType: DataStoreSourceType
    ) {
        getDataSource(dataStoreSourceType).edit { preferences ->
            preferences[key] = value
        }
    }

    override suspend fun <T> updatePreference(
        key: Preferences.Key<T>,
        dataStoreSourceType: DataStoreSourceType,
        transform: (T?) -> T
    ) {
        getDataSource(dataStoreSourceType).edit { preferences ->
            preferences[key] = transform(preferences[key])
        }
    }

    override suspend fun <T> removePreference(key: Preferences.Key<T>, dataStoreSourceType: DataStoreSourceType) {
        getDataSource(dataStoreSourceType).edit { preferences ->
            preferences.remove(key)
        }
    }
}

/** IO failures reading a store surface as an empty snapshot, so reads fall back to their defaults. */
private fun Flow<Preferences>.orEmptyOnIoError(): Flow<Preferences> = catch { exception ->
    if (exception is IOException) {
        emit(emptyPreferences())
    } else {
        throw exception
    }
}
