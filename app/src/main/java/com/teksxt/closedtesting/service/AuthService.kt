package com.teksxt.closedtesting.service

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.teksxt.closedtesting.profile.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    // The currentUser property will return:
    // 1. FirebaseUser object - when a user is successfully authenticated and signed in
    // 2. null - when no user is signed in or authentication has failed
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return firebaseAuth.signInWithCredential(credential).await()
    }

    suspend fun updateUserProfile(displayName: String, photoUrl: String? = null) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .apply {
                photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) }
            }
            .build()
        currentUser?.updateProfile(profileUpdates)?.await()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUserAsModel(): User? {
        return currentUser?.let {
            User(
                id = it.uid,
                name = it.displayName ?: "",
                email = it.email ?: "",
                photoUrl = it.photoUrl?.toString()
            )
        }
    }

    suspend fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }
}