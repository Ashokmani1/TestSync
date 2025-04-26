package com.teksxt.closedtesting.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMTokenManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) {
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    fun registerFCMToken() {
        try {
            auth.addAuthStateListener { firebaseAuth ->

                val userId = firebaseAuth.currentUser?.uid ?: return@addAuthStateListener
                // Get the current FCM token
                serviceScope.launch {
                    val token = FirebaseMessaging.getInstance().token.await()
                    // Update the user's token in Firestore
                    userRepository.updateFCMToken(userId, token)
                }
            }

        } catch (e: Exception) {
            Log.e("FCMTokenManager", "Failed to register FCM token: ${e.message}")
        }
    }
}