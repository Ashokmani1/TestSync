package com.teksxt.closedtesting.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.teksxt.closedtesting.data.preferences.UserPreferencesManager
import com.teksxt.closedtesting.domain.model.UserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val userPrefsManager: UserPreferencesManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Expose user preferences
    val isLoggedIn: Flow<Boolean> = userPrefsManager.isLoggedIn
    val userEmail: Flow<String?> = userPrefsManager.userEmail

    init {
        // Initialize session state from Firebase Auth
        auth.addAuthStateListener { firebaseAuth ->
            scope.launch {
                val user = firebaseAuth.currentUser
                if (user != null) {
                    refreshIdToken(user)
                    userPrefsManager.saveUserId(user.uid)
                    userPrefsManager.saveUserEmail(user.email)
                    userPrefsManager.setLoggedIn(true)
                    _authState.value = AuthState.Authenticated(user.toUserModel())
                } else {
                    userPrefsManager.clearUserData()
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    suspend fun refreshSession(): Boolean {
        val currentUser = auth.currentUser ?: return false
        return try {
            currentUser.reload().await()
            refreshIdToken(currentUser)
            _authState.value = AuthState.Authenticated(currentUser.toUserModel())
            true
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to refresh session")
            false
        }
    }

    private suspend fun refreshIdToken(user: FirebaseUser) {
        try {
            // Get fresh ID token for API calls
            val token = user.getIdToken(true).await().token
            userPrefsManager.saveAuthToken(token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun signOut() {
        auth.signOut()
        userPrefsManager.clearUserData()
        _authState.value = AuthState.Unauthenticated
    }

    private fun FirebaseUser.toUserModel() = UserModel(
        uid = uid,
        email = email ?: "",
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
        isEmailVerified = isEmailVerified
    )
}

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: UserModel) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}