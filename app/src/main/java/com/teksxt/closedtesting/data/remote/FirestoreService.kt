package com.teksxt.closedtesting.data.remote

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.teksxt.closedtesting.data.remote.model.UserDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Collections
    private val usersCollection = firestore.collection("users")


    // User operations
    suspend fun createUser(userId: String, userData: UserDto) {
        usersCollection.document(userId).set(userData).await()
    }
    
    suspend fun updateUser(userId: String, userData: Map<String, Any>) {
        usersCollection.document(userId).update(userData).await()
    }
    
    suspend fun getUserById(userId: String): UserDto? {
        val document = usersCollection.document(userId).get().await()
        return if (document.exists()) {
            val userData = document.toObject(UserDto::class.java)
            userData?.copy(id = document.id, data = document.data ?: mapOf(), photoUrl = document.getString("photoUrl"))
        } else null
    }

    fun listenToUserPreferences(
        userId: String,
        callback: (Map<String, Any>?) -> Unit
    ): ListenerRegistration
    {
        return usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                callback(snapshot?.data)
            }
    }

    /**
     * Uploads an image to Firebase Storage and returns the download URL
     * @param userId User ID to organize images by user
     * @param localImageUri Local URI of the image to upload
     * @return Download URL of the uploaded image
     */
    suspend fun uploadImageToStorage(userId: String, localImageUri: String): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_images/$userId/profile_image.jpg")
        
        // Convert URI string to Uri object
        val fileUri = Uri.parse(localImageUri)
        
        // Upload the file
        val uploadTask = imageRef.putFile(fileUri)
        
        // Wait for upload to complete and get download URL
        return uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imageRef.downloadUrl
        }.await().toString()
    }
}