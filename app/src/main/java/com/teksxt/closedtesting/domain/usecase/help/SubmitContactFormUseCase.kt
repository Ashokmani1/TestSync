package com.teksxt.closedtesting.domain.usecase.help

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teksxt.closedtesting.core.util.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmitContactFormUseCase @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        subject: String,
        message: String
    ): Resource<Unit> {
        return try {
            // Validate inputs
            if (name.isBlank()) {
                return Resource.Error("Name cannot be empty")
            }

            if (email.isBlank() || !isValidEmail(email)) {
                return Resource.Error("Please enter a valid email address")
            }

            if (subject.isBlank()) {
                return Resource.Error("Subject cannot be empty")
            }

            if (message.isBlank()) {
                return Resource.Error("Message cannot be empty")
            }

            // Create contact form data
            val contactForm = hashMapOf(
                "name" to name,
                "email" to email,
                "subject" to subject,
                "message" to message,
                "userId" to auth.uid,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "status" to "pending"
            )

            // Submit to Firestore
            firestore.collection("support_tickets")
                .add(contactForm)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        return email.matches(emailRegex)
    }
}