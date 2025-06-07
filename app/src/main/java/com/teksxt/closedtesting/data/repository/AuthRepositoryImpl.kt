package com.teksxt.closedtesting.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.teksxt.closedtesting.data.auth.GoogleSignInHelper
import com.teksxt.closedtesting.data.local.TestSyncDatabase
import com.teksxt.closedtesting.domain.repository.AuthRepository
import com.teksxt.closedtesting.presentation.auth.SessionManager
import com.teksxt.closedtesting.service.FCMTokenManager
import com.teksxt.closedtesting.settings.domain.model.User
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInHelper: GoogleSignInHelper,
    private val userRepository: UserRepository,
    private val fcmTokenManager: FCMTokenManager,
    private val sessionManager: SessionManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val database: TestSyncDatabase
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->

                // Use UserRepository to ensure both Firestore and Room are updated
                val existingUser = userRepository.getUserById(user.uid).getOrNull()
                if (existingUser != null) {
                    // Update last login time
                    userRepository.updateUser(existingUser.copy(
                        lastActive = FieldValue.serverTimestamp()
                    ))
                } else {
                    // Create new user if not found
                    val newUser = User(
                        id = user.uid,
                        email = user.email ?: "",
                        name = user.displayName ?: email.substringBefore('@'),
                        photoUrl = user.photoUrl?.toString(),
                        createdAt = FieldValue.serverTimestamp(),
                        isOnboarded = false
                    )
                    userRepository.createUser(newUser)
                }

                fcmTokenManager.registerFCMToken()

                sessionManager.startTrackingUserActivity()

                // Trigger sync to ensure data consistency
                userRepository.syncUserData()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(name: String, email: String, password: String): Result<Unit> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Update Firebase Auth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Create user in both Firestore and Room via UserRepository
                val newUser = User(
                    id = user.uid,
                    email = email,
                    name = name,
                    photoUrl = null,
                    createdAt = FieldValue.serverTimestamp(),
                    isOnboarded = false,
                    termsAccepted = true
                )
                userRepository.createUser(newUser)

                fcmTokenManager.registerFCMToken()

                sessionManager.startTrackingUserActivity()

                // Send verification email
                user.sendEmailVerification().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            authResult.user?.let { user ->
                // Update or create user
                val existingUser = userRepository.getUserById(user.uid).getOrNull()
                if (existingUser != null) {
                    // Update existing user
                    userRepository.updateUser(existingUser.copy(
                        email = user.email ?: existingUser.email,
                        name = user.displayName ?: existingUser.name,
                        photoUrl = user.photoUrl?.toString() ?: existingUser.photoUrl,
                        lastActive = FieldValue.serverTimestamp()
                    ))
                } else {
                    // Create new user
                    val newUser = User(
                        id = user.uid,
                        email = user.email ?: "",
                        name = user.displayName ?: "",
                        photoUrl = user.photoUrl?.toString(),
                        createdAt = FieldValue.serverTimestamp(),
                        isOnboarded = false,
                        termsAccepted = false
                    )
                    userRepository.createUser(newUser)
                }

                fcmTokenManager.registerFCMToken()
                sessionManager.startTrackingUserActivity()
                userRepository.syncUserData()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google sign-in error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogleSilent(): Result<Unit> {
        return try {
            // Check if user has previously signed in
            if (!googleSignInHelper.hasPreviousSignIn()) {
                Log.d("AuthRepository", "No previous Google sign-in detected")
                return Result.failure(SilentSignInFailedException())
            }

            // Try to get ID token
            val idToken = googleSignInHelper.getIdToken()

            if (idToken != null) {
                Log.d("AuthRepository", "Got valid ID token, authenticating with Firebase")

                // Use the existing signInWithGoogle method
                return signInWithGoogle(idToken)
            } else {
                Log.w("AuthRepository", "Silent sign-in failed: No valid ID token")
                // Clear any potentially stale state
                googleSignInHelper.signOut()
                return Result.failure(SilentSignInFailedException())
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during silent Google sign-in: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // TODO notication clear while logout
    override suspend fun logout(): Result<Unit> {
        return try {
            // First sync any pending local changes
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                try {
                    userRepository.syncUserData()
                } catch (_: Exception) {
                    // Continue with logout even if sync fails
                }
            }

            sessionManager.stopTrackingUserActivity()

            withContext(Dispatchers.IO) {
                // Clear all tables in the database
                database.clearAllTables()
            }

            userPreferencesRepository.clearAllPreferences()

            firebaseAuth.signOut()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getCurrentUser(): User? {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return null
        return userRepository.getUserById(currentUserId).getOrNull()
    }


    override suspend fun sendEmailVerificationLink(): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("User not signed in"))

            currentUser.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isEmailVerified(): Result<Boolean> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("User not signed in"))

            // This returns the cached value, might not be up-to-date
            val isVerified = currentUser.isEmailVerified
            Result.success(isVerified)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshCurrentUser(): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("User not signed in"))

            // This reloads the user details from Firebase, including verification status
            currentUser.reload().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("User not signed in"))

            val userId = currentUser.uid

            // 1. First make sure we have the latest auth session
            currentUser.reload().await()

            // 2. Some actions like account deletion require recent authentication
            // If it fails due to credential timeout, we'll return an error
            // that the UI can handle by prompting for re-authentication
            try {
                // 3. Delete the Firebase Auth user
                currentUser.delete().await()

                // 4. Attempt to clean up Firestore data
                // (This should ideally be handled by Firebase Functions for security)
//                try {
//                    // Delete user document
//                    firestore.collection("users").document(userId).delete().await()
//
//                    // Note: In a production app, you would use Firebase Cloud Functions
//                    // to securely delete all of the user's data from Firestore
//                } catch (e: Exception) {
//                    Log.e("AuthRepository", "Error cleaning up Firestore data: ${e.message}")
//                    // Continue with the process even if Firestore cleanup fails
//                }

                // 5. Clean up local data
                withContext(Dispatchers.IO) {
                    database.clearAllTables()
                }

                // 6. Clear preferences
                userPreferencesRepository.clearAllPreferences()

                Result.success(Unit)
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                // This exception means the user needs to re-authenticate before
                // sensitive operations like account deletion
                Result.failure(Exception("Please sign in again before deleting your account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}

class SilentSignInFailedException : Exception("Silent sign-in failed, interactive sign-in required")