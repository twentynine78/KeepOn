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
import kotlinx.coroutines.flow.first
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

interface PreferenceDataStoreHelper {
    suspend fun <T> putPreference(key: Preferences.Key<T>, value: T, dataStoreSourceType: DataStoreSourceType)
    suspend fun <T> removePreference(key: Preferences.Key<T>, dataStoreSourceType: DataStoreSourceType)
    fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T, dataStoreSourceType: DataStoreSourceType): Flow<T>
    fun <T> getPreference(key: Preferences.Key<T>, dataStoreSourceType: DataStoreSourceType): Flow<T?>
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

class PreferenceDataStoreHelperImpl @Inject constructor(@param:ApplicationContext private val context: Context) : PreferenceDataStoreHelper {

    private fun getDataSource(dataStoreSourceType: DataStoreSourceType): DataStore<Preferences> {
        return when (dataStoreSourceType) {
            DataStoreSourceType.DATA_SOURCE_BACKED_UP -> context.dataStoreBackedUp
            DataStoreSourceType.DATA_SOURCE -> context.dataStore
        }
    }

    override fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T, dataStoreSourceType: DataStoreSourceType):
        Flow<T> = getDataSource(dataStoreSourceType).data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        val result = preferences[key] ?: defaultValue
        result
    }

    override fun <T> getPreference(key: Preferences.Key<T>, dataStoreSourceType: DataStoreSourceType):
        Flow<T?> = getDataSource(dataStoreSourceType).data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[key]
    }

    // Returns the last saved value of the key
    override suspend fun <T> getLastPreference(
        key: Preferences.Key<T>,
        defaultValue: T,
        dataStoreSourceType: DataStoreSourceType
    ): T = getDataSource(dataStoreSourceType).data.first()[key] ?: defaultValue

    override suspend fun <T> getLastPreference(
        key: Preferences.Key<T>,
        dataStoreSourceType: DataStoreSourceType
    ): T? = getDataSource(dataStoreSourceType).data.first()[key]

    // Sets the value based on the value passed in value parameter
    override suspend fun <T> putPreference(
        key: Preferences.Key<T>,
        value: T,
        dataStoreSourceType: DataStoreSourceType
    ) {
        getDataSource(dataStoreSourceType).edit { preferences ->
            preferences[key] = value
        }
    }

    // This Function removes the Key Value pair from the datastore, hereby removing it completely.
    override suspend fun <T> removePreference(key: Preferences.Key<T>, dataStoreSourceType: DataStoreSourceType) {
        getDataSource(dataStoreSourceType).edit { preferences ->
            preferences.remove(key)
        }
    }
}
