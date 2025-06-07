package com.teksxt.closedtesting.presentation.help

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.domain.repository.AuthRepository
import com.teksxt.closedtesting.notifications.domain.repository.NotificationRepository
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PrivacyEvent>()
    val events = _events.asSharedFlow()

    init {
        loadPrivacyPolicy()
    }

    fun showDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    // TODO delete account is stil pending.
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    isLoading = true,
                    showDeleteDialog = false
                )}

                // 1. First clean up user data in Firestore
                val userId = authRepository.getCurrentUser()?.id
                if (userId != null) {
                    // Delete user's notifications
                    notificationRepository.clearAllNotifications()

                    // Delete user's other data (requests, apps, feedback, etc.)
                    // This would normally call other repositories to clean up all user data
                }

                // 2. Delete the Firebase Auth account
                val result = authRepository.deleteAccount()

                if (result.isSuccess) {
                    // Clear any remaining local data and preferences
                    authRepository.logout()

                    // Navigate to login screen
                    _events.emit(PrivacyEvent.NavigateToLogin)
                } else {
                    // Show error
                    val exception = result.exceptionOrNull()
                    _uiState.update { it.copy(
                        error = "Could not delete account: ${exception?.message ?: "Unknown error"}",
                        isLoading = false
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to delete account: ${e.message}",
                    isLoading = false
                )}
            }
        }
    }

    private fun loadPrivacyPolicy() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulate network delay
            delay(500)

            // In a real app, this would fetch from a repository
            val sections = listOf(
                PrivacySection(
                    title = "1. Introduction",
                    content = "This Privacy Policy explains how TestSync collects, uses, and discloses information about you when you use our mobile application and related services. We are committed to protecting your privacy and the security of your personal information."
                ),
                PrivacySection(
                    title = "2. Information We Collect",
                    content = "We collect information that you provide directly, including account information (name, email, password), profile details (testing preferences, device information), and content you submit (feedback, screenshots, messages). We also collect usage data, device information, and analytics to improve our service."
                ),
                PrivacySection(
                    title = "3. How We Use Your Information",
                    content = "We use your information to provide and improve our service, match testers with appropriate apps, process and display feedback, communicate with you about the service, and ensure security and compliance with our terms."
                ),
                PrivacySection(
                    title = "4. Information Sharing",
                    content = "We share information between app owners and testers as necessary for the testing process. We may also share with service providers who perform services on our behalf, and in response to legal requirements or to protect rights and safety."
                ),
                PrivacySection(
                    title = "5. Data Security",
                    content = "We implement appropriate technical and organizational measures to protect your personal information against unauthorized access, disclosure, alteration, and destruction."
                ),
                PrivacySection(
                    title = "6. Your Rights and Choices",
                    content = "You have the right to access, correct, or delete your personal information. You can update most information through your account settings, request data export, or delete your account entirely."
                ),
                PrivacySection(
                    title = "7. Children's Privacy",
                    content = "Our service is not directed to children under the age of 13. We do not knowingly collect personal information from children under 13. If we learn we have collected information from a child under 13, we will delete that information."
                ),
                PrivacySection(
                    title = "8. International Data Transfers",
                    content = "Your information may be transferred to and processed in countries other than your own. We ensure appropriate safeguards are in place to protect your information when transferred internationally."
                ),
                PrivacySection(
                    title = "9. Changes to This Policy",
                    content = "We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy on this page and updating the effective date at the top of this page."
                ),
                PrivacySection(
                    title = "10. Contact Us",
                    content = "If you have questions about this Privacy Policy or our privacy practices, please contact us at privacy@testsync.com."
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    sections = sections,
                    effectiveDate = "April 12, 2025",
                    version = ""
                )
            }
        }
    }

    fun clearNotifications(context: Context)
    {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

}

sealed class PrivacyEvent {
    object NavigateToLogin : PrivacyEvent()
}

data class PrivacyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sections: List<PrivacySection> = emptyList(),
    val effectiveDate: String = "",
    val version: String = "",
    val showDeleteDialog: Boolean = false
)

data class PrivacySection(
    val title: String,
    val content: String
)