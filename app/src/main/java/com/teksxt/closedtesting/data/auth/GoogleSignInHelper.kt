package com.teksxt.closedtesting.data.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.teksxt.closedtesting.R
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


class GoogleSignInHelper @Inject constructor(
    private val context: Context
) {
    // Configure Google Sign-In options
    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    // Create GoogleSignInClient
    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get the last signed-in account without showing UI
     * @return The GoogleSignInAccount if available, or null if not
     */
    suspend fun getLastSignedInAccount(): GoogleSignInAccount? {
        return try {
            // Check if the user has already signed in with Google
            val account = GoogleSignIn.getLastSignedInAccount(context)
            
            // If we have an account but it might be expired, silently refresh
            if (account != null && !account.isExpired) {
                account
            } else {
                // Try to silently sign in
                val task = googleSignInClient.silentSignIn().await()
                task
            }
        } catch (e: ApiException) {
            // Handle error
            null
        } catch (e: Exception) {
            // Handle other exceptions
            null
        }
    }
    
    /**
     * Get the ID token for the current Google account
     * @return The ID token if available, or null if not
     */
    suspend fun getIdToken(): String? {
        return getLastSignedInAccount()?.idToken
    }

    /**
     * Signs out from the Google account
     */
    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            // Handle sign out error
        }
    }
}