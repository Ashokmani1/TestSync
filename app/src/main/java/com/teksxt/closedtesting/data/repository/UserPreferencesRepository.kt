package com.teksxt.closedtesting.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys for preferences
    private object PreferencesKeys {
        val EMAIL_VERIFICATION_BANNER_DISMISSED = booleanPreferencesKey("email_verification_banner_dismissed")
        val EMAIL_VERIFICATION_LAST_SENT_TIMESTAMP = longPreferencesKey("email_verification_last_sent_timestamp")
        val TERMS_ACCEPTED_TIMESTAMP = longPreferencesKey("terms_accepted_timestamp")
        val IS_TERMS_ACCEPTED = booleanPreferencesKey("is_terms_accepted")
        val IS_TERMS_ACCEPTED_VERSION = stringPreferencesKey("is_terms_accepted_version")
    }

    // Get the banner dismissed state as a Flow
    val emailVerificationBannerDismissed: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EMAIL_VERIFICATION_BANNER_DISMISSED] ?: false
        }

    // Get the last verification sent timestamp as a Flow
    val verificationLastSentTimestamp: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EMAIL_VERIFICATION_LAST_SENT_TIMESTAMP] ?: 0L
        }

    // Save banner dismissed state
    suspend fun setEmailVerificationBannerDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL_VERIFICATION_BANNER_DISMISSED] = dismissed
        }
    }

    // Save last verification sent timestamp
    suspend fun setVerificationLastSentTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL_VERIFICATION_LAST_SENT_TIMESTAMP] = timestamp
        }
    }

    val isTermsAccepted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_TERMS_ACCEPTED] ?: false
        }

    suspend fun setTermsAccepted(accepted: Boolean)
    {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_TERMS_ACCEPTED] = accepted
        }
    }

    suspend fun setAcceptedTermsVersion(version: String)
    {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_TERMS_ACCEPTED_VERSION] = version
        }
    }

    fun getAcceptedTermsVersion(): Flow<String>
    {
        return context.dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.IS_TERMS_ACCEPTED_VERSION] ?: ""
            }
    }

    suspend fun setTermsAcceptanceTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TERMS_ACCEPTED_TIMESTAMP] = timestamp
        }
    }


    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}