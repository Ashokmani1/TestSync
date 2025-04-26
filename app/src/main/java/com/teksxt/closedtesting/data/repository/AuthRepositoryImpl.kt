package com.teksxt.closedtesting.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.teksxt.closedtesting.data.auth.GoogleSignInHelper
import com.teksxt.closedtesting.data.preferences.UserPreferences
import com.teksxt.closedtesting.domain.repository.AuthRepository
import com.teksxt.closedtesting.presentation.auth.SessionManager
import com.teksxt.closedtesting.service.FCMTokenManager
import com.teksxt.closedtesting.settings.domain.model.User
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferences: UserPreferences,
    private val googleSignInHelper: GoogleSignInHelper,
    private val userRepository: UserRepository,
    private val fcmTokenManager: FCMTokenManager,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                userPreferences.saveUserId(user.uid)

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

                userPreferences.saveUserId(user.uid)

                // Create user in both Firestore and Room via UserRepository
                val newUser = User(
                    id = user.uid,
                    email = email,
                    name = name,
                    photoUrl = null,
                    createdAt = FieldValue.serverTimestamp(),
                    isOnboarded = false
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
                userPreferences.saveUserId(user.uid)

                // Use UserRepository to update or create user
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
                        isOnboarded = false
                    )
                    userRepository.createUser(newUser)
                }

                fcmTokenManager.registerFCMToken()

                sessionManager.startTrackingUserActivity()

                // Ensure data consistency
                userRepository.syncUserData()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogleSilent(): Result<Unit> {
        return try {
            // Try to get ID token from the last signed-in Google account
            val idToken = googleSignInHelper.getIdToken()
            
            if (idToken != null) {
                // We have a valid ID token, use it to authenticate with Firebase
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = firebaseAuth.signInWithCredential(credential).await()
                
                result.user?.let { user ->

                    userPreferences.saveUserId(user.uid)
                    
                    // Get or create user profile
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
                            name = user.displayName ?: (user.email ?: "").substringBefore('@'),
                            photoUrl = user.photoUrl?.toString(),
                            createdAt = FieldValue.serverTimestamp(),
                            isOnboarded = false
                        )
                        userRepository.createUser(newUser)
                    }
                }

                fcmTokenManager.registerFCMToken()
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Silent sign-in failed"))
            }
        } catch (e: Exception) {
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

            // TODO notication clear while logout
            // Clear local user data
            firebaseAuth.signOut()
//            userPreferences.clear() // TODO chekc this

            // If we have a UserDao method to clear user cache, call it here
            // userRepository.clearUserCache()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getCurrentUser(): User? {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return null
        return userRepository.getUserById(currentUserId).getOrNull()
    }

}