package com.shayo.moviespoint.core.usage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val USAGE_ENABLED = booleanPreferencesKey("usage_enabled")

internal val SHOULD_ASK = booleanPreferencesKey("should_asl")

internal class UsageServiceImpl @OptIn(DelicateCoroutinesApi::class) constructor(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>,
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
    private val analytics: FirebaseAnalytics = Firebase.analytics,
    private val scope: CoroutineScope = GlobalScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UsageService {

    override fun changeUsage(enabled: Boolean) {
        scope.launch(dispatcher) {
            crashlytics.setCrashlyticsCollectionEnabled(enabled)

            analytics.setAnalyticsCollectionEnabled(enabled)

            dataStore.edit { settings ->
                settings[USAGE_ENABLED] = enabled
            }
        }
    }


    override val crashEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[USAGE_ENABLED] ?: true
        }

    override val shouldAskFlow = dataStore.data
        .map { preferences ->
            preferences[SHOULD_ASK] ?: true
        }

    override fun markAsked() {
        scope.launch(dispatcher) {
            dataStore.edit { settings ->
                settings[SHOULD_ASK] = false
            }
        }
    }
}