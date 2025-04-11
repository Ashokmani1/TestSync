package com.teksxt.closedtesting.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.teksxt.closedtesting.data.auth.GoogleSignInHelper
import com.teksxt.closedtesting.data.preferences.UserPreferences
import com.teksxt.closedtesting.data.remote.FirestoreService
import com.teksxt.closedtesting.data.remote.model.UserDto
import com.teksxt.closedtesting.profile.domain.model.User
import com.teksxt.closedtesting.profile.domain.model.UserType
import com.teksxt.closedtesting.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreService: FirestoreService,
    private val userPreferences: UserPreferences,
    private val googleSignInHelper: GoogleSignInHelper
) : AuthRepository
{

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                userPreferences.saveUserId(user.uid)
                
                // Update last login time
                val userDto = firestoreService.getUserById(user.uid) ?: UserDto(
                    id = user.uid,
                    email = user.email ?: "",
                    name = user.displayName ?: ""
                )
                firestoreService.createUser(user.uid, userDto)
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
                // Create user profile
                val newUserDto = UserDto(
                    id = user.uid,
                    email = email,
                    name = name,
                    photoUrl = null,
                    userType = UserType.UNKNOWN.name,
                    isOnboarded = false
                )
                firestoreService.createUser(user.uid, newUserDto)
                userPreferences.saveUserId(user.uid)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            
            result.user?.let { user ->
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false
                userPreferences.saveUserId(user.uid)
                
                // Create or update user profile
                val userDto = if (isNewUser) {
                    UserDto(
                        id = user.uid,
                        email = user.email ?: "",
                        name = user.displayName ?: "",
                        photoUrl = user.photoUrl?.toString(),
                        isOnboarded = false
                    )
                } else {
                    firestoreService.getUserById(user.uid) ?: UserDto(
                        id = user.uid,
                        email = user.email ?: "",
                        name = user.displayName ?: "",
                        photoUrl = user.photoUrl?.toString()
                    )
                }
                firestoreService.createUser(user.uid, userDto)
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
                    val userDto = firestoreService.getUserById(user.uid) ?: UserDto(
                        id = user.uid,
                        email = user.email ?: "",
                        name = user.displayName ?: "",
                        photoUrl = user.photoUrl?.toString()
                    )
                    firestoreService.createUser(user.uid, userDto)
                }
                
                Result.success(Unit)
            } else {
                // No valid ID token found
                Result.failure(Exception("No valid Google credentials found. Please sign in with Google."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            userPreferences.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        
        // Try to get from Firestore
        val firestoreUser = firestoreService.getUserById(firebaseUser.uid)
        
        return firestoreUser?.let {
            User(
                id = it.id,
                email = it.email,
                name = it.name,
                photoUrl = it.photoUrl,
                userType = try { 
                    UserType.valueOf(it.userType)
                } catch (e: Exception) { 
                    UserType.UNKNOWN
                },
                isOnboarded = it.isOnboarded
            )
        } ?: run {
            // Fallback to Firebase user data
            User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                name = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString(),
                userType = UserType.UNKNOWN,
                isOnboarded = false
            )
        }
    }

    override fun observeAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}