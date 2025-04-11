package com.teksxt.closedtesting.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor(
    private val storage: FirebaseStorage
) {
    companion object {
        private const val SCREENSHOTS_PATH = "screenshots"
        private const val LOGS_PATH = "logs"
        private const val PROFILE_IMAGES_PATH = "profile_images"
        private const val APP_IMAGES_PATH = "app_images"
    }

    private val storageRef: StorageReference = storage.reference

    /**
     * Uploads a screenshot for a test report
     */
    suspend fun uploadScreenshot(imageUri: Uri, testReportId: String): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val screenshotRef = storageRef.child("$SCREENSHOTS_PATH/$testReportId/$fileName")
        
        return uploadFile(screenshotRef, imageUri)
    }

    /**
     * Uploads a log file for a test report
     */
    suspend fun uploadLogFile(fileUri: Uri, testReportId: String): String {
        val fileName = "${UUID.randomUUID()}.txt"
        val logRef = storageRef.child("$LOGS_PATH/$testReportId/$fileName")
        
        return uploadFile(logRef, fileUri)
    }

    /**
     * Uploads a profile image for a user
     */
    suspend fun uploadProfileImage(imageUri: Uri, userId: String): String {
        val profileRef = storageRef.child("$PROFILE_IMAGES_PATH/$userId/profile.jpg")
        
        return uploadFile(profileRef, imageUri)
    }

    /**
     * Uploads an app screenshot for a test request
     */
    suspend fun uploadAppImage(imageUri: Uri, testRequestId: String): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val appImageRef = storageRef.child("$APP_IMAGES_PATH/$testRequestId/$fileName")
        
        return uploadFile(appImageRef, imageUri)
    }

    /**
     * Generic file upload function
     */
    private suspend fun uploadFile(storageRef: StorageReference, fileUri: Uri): String {
        storageRef.putFile(fileUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    /**
     * Deletes a file from Firebase Storage
     */
    suspend fun deleteFile(fileUrl: String) {
        try {
            val httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
            httpsReference.delete().await()
        } catch (e: Exception) {
            // Log and handle the error
            e.printStackTrace()
        }
    }
}