package com.teksxt.closedtesting.data.auth

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.teksxt.closedtesting.R
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleSignInHelper @Inject constructor(
    private val context: Context
) {
    // Configure Google Sign-In options
    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile() // Request profile data as well
            .build()
    }

    // Create GoogleSignInClient
    val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get ID token from Google Sign-In
     * @return The ID token if available, null otherwise
     */
     suspend fun getIdToken(): String? {
        return try {
            // First check if there's an existing signed-in account
            val lastAccount = GoogleSignIn.getLastSignedInAccount(context)

            if (lastAccount != null && !lastAccount.isExpired) {
                Log.d("GoogleSignInHelper", "Using existing account: ${lastAccount.email}")
                val token = lastAccount.idToken
                if (!token.isNullOrBlank()) {
                    return token
                }
            }

            // If we don't have a valid token from the last account, try silent sign-in
            Log.d("GoogleSignInHelper", "No valid token from last account, trying silent sign-in")
            val silentSignInTask = googleSignInClient.silentSignIn()

            try {
                val account = silentSignInTask.await()
                Log.d("GoogleSignInHelper", "Silent sign-in successful for: ${account.email}")
                return account.idToken
            } catch (e: ApiException) {
                Log.e("GoogleSignInHelper", "Silent sign-in ApiException: ${e.statusCode}", e)
                return null
            } catch (e: Exception) {
                Log.e("GoogleSignInHelper", "Silent sign-in error: ${e.message}", e)
                return null
            }
        } catch (e: Exception) {
            Log.e("GoogleSignInHelper", "Error retrieving ID token: ${e.message}", e)
            return null
        }
    }

    fun hasPreviousSignIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
    
    /**
     * Sign out from Google
     */
    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
            Log.d("GoogleSignInHelper", "Google sign-out successful")
        } catch (e: Exception) {
            Log.e("GoogleSignInHelper", "Google sign-out failed: ${e.message}")
        }
    }
    
    /**
     * Revoke Google access
     */
    suspend fun revokeAccess() {
        try {
            googleSignInClient.revokeAccess().await()
            Log.d("GoogleSignInHelper", "Google access revoked")
        } catch (e: Exception) {
            Log.e("GoogleSignInHelper", "Failed to revoke Google access: ${e.message}")
        }
    }
}