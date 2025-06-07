package com.teksxt.closedtesting.presentation.help

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.teksxt.closedtesting.TestSyncApp
import com.teksxt.closedtesting.data.repository.UserPreferencesRepository
import com.teksxt.closedtesting.domain.repository.AuthRepository
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import com.teksxt.closedtesting.util.DeviceInfoProvider
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TermsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TermsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TermsEvent>()
    val events = _events.asSharedFlow()

    init {
        loadTerms()
        checkTermsAcceptanceStatus()
    }

    fun acceptTerms() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isAccepting = true) }

                // 1. Get current user
                val userId = authRepository.getCurrentUser()?.id

                // 2. Create acceptance record with metadata
                val termsAcceptance = hashMapOf(
                    "version" to uiState.value.version,
                    "acceptedAt" to FieldValue.serverTimestamp(),
                    "deviceInfo" to getDeviceInfo()
                )

                // 3. Record in Firestore (if logged in)
                if (userId != null) {
                    userRepository.updateTermsAcceptance(userId, termsAcceptance)
                        .onSuccess {
                            Log.d("TermsViewModel", "Terms acceptance recorded for user $userId")
                        }
                        .onFailure { e ->
                            Log.e("TermsViewModel", "Failed to record terms acceptance: ${e.message}", e)
                        }
                }

                // 4. Always store locally (for both logged-in and anonymous users)
                userPreferencesRepository.setTermsAccepted(true)
                userPreferencesRepository.setAcceptedTermsVersion(uiState.value.version)
                userPreferencesRepository.setTermsAcceptanceTimestamp(System.currentTimeMillis())

                // 5. Update UI state
                _uiState.update {
                    it.copy(
                        isAccepting = false,
                        hasAcceptedTerms = true,
                        showAcceptButton = false
                    )
                }

                // 6. Emit success event
                _events.emit(TermsEvent.TermsAccepted)

            } catch (e: Exception) {
                Log.e("TermsViewModel", "Error accepting terms: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isAccepting = false,
                        error = "Could not save terms acceptance: ${e.message}"
                    )
                }
            }
        }
    }

    private fun checkTermsAcceptanceStatus() {
        viewModelScope.launch {
            try {
                // 1. First check local preferences for faster response
                userPreferencesRepository.isTermsAccepted
                    .first()
                    .let { accepted ->
                        if (accepted) {
                            // Check if the accepted version matches current version
                            val acceptedVersion = userPreferencesRepository.getAcceptedTermsVersion().first()
                            val isCurrentVersion = acceptedVersion == uiState.value.version

                            _uiState.update {
                                it.copy(
                                    hasAcceptedTerms = isCurrentVersion,
                                    showAcceptButton = !isCurrentVersion
                                )
                            }
                        } else {
                            // If not accepted locally, show the accept button
                            _uiState.update { it.copy(showAcceptButton = true) }
                        }
                    }

                // 2. If logged in, double-check with server data
                val userId = authRepository.getCurrentUser()?.id
                if (userId != null) {
                    userRepository.getUserTermsAcceptance(userId)
                        .onSuccess { serverAcceptance ->
                            if (serverAcceptance != null) {
                                val serverVersion = serverAcceptance["version"] as? String
                                val isCurrentVersion = serverVersion == uiState.value.version

                                _uiState.update {
                                    it.copy(
                                        hasAcceptedTerms = isCurrentVersion,
                                        showAcceptButton = !isCurrentVersion
                                    )
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("TermsViewModel", "Error checking terms status: ${e.message}", e)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun getDeviceInfo(): Map<String, String> {
        return try {
            val context = TestSyncApp.instance
            mapOf(
                "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}",
                "osVersion" to "Android ${Build.VERSION.RELEASE}",
                "appVersion" to try {
                    DeviceInfoProvider(context).getAppVersion()
                } catch (e: Exception) {
                    "Unknown"
                }
            )
        } catch (e: Exception) {
            mapOf("error" to "Could not collect device info")
        }
    }

    private fun loadTerms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Simulate network delay
            delay(500)

            // In a real app, this would fetch from a repository
            val sections = listOf(
                TermsSection(
                    title = "1. Acceptance of Terms",
                    content = "By accessing or using the TestSync platform, you agree to be bound by these Terms of Service. If you disagree with any part of the terms, you do not have permission to access the service."
                ),
                TermsSection(
                    title = "2. Description of Service",
                    content = "TestSync provides a platform for app developers to connect with testers for the purpose of evaluating mobile applications before their official launch. The platform facilitates the creation of testing groups, manages feedback collection, and helps improve app quality."
                ),
                TermsSection(
                    title = "3. User Account Responsibilities",
                    content = "When you create an account with us, you must provide accurate and complete information. You are solely responsible for maintaining the confidentiality of your account and password. You agree to accept responsibility for all activities that occur under your account."
                ),
                TermsSection(
                    title = "4. App Submission Guidelines",
                    content = "App owners who submit applications for testing must ensure their apps do not contain malware, do not violate intellectual property rights, and do not collect user data beyond what is disclosed. Apps must comply with all applicable laws and platform policies."
                ),
                TermsSection(
                    title = "5. Tester Conduct",
                    content = "Testers agree to provide honest feedback, maintain confidentiality regarding pre-release apps, and complete testing tasks within the specified timeframes. Testers may not reverse engineer apps or use them for any purpose other than testing."
                ),
                TermsSection(
                    title = "6. Intellectual Property",
                    content = "All content on the TestSync platform, including but not limited to text, graphics, logos, and software, is the property of TestSync or its content suppliers and is protected by copyright and other intellectual property laws."
                ),
                TermsSection(
                    title = "7. Limitation of Liability",
                    content = "TestSync shall not be liable for any indirect, incidental, special, consequential, or punitive damages resulting from your access to or use of, or inability to access or use, the service or any content provided on the service."
                ),
                TermsSection(
                    title = "8. Termination",
                    content = "We may terminate or suspend your account immediately, without prior notice or liability, for any reason, including if you breach the Terms of Service. Upon termination, your right to use the service will immediately cease."
                ),
                TermsSection(
                    title = "9. Changes to Terms",
                    content = "We reserve the right to modify or replace these terms at any time. If a revision is material, we will try to provide at least 30 days' notice prior to any new terms taking effect."
                ),
                TermsSection(
                    title = "10. Contact Us",
                    content = "If you have any questions about these Terms, please contact us at support@testsync.com."
                )
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    sections = sections,
                    effectiveDate = "April 12, 2025",
                    version = "1.2"
                )
            }
        }
    }
}

sealed class TermsEvent {
    object TermsAccepted : TermsEvent()
}

// Update the UI state
data class TermsUiState(
    val isLoading: Boolean = false,
    val isAccepting: Boolean = false,
    val sections: List<TermsSection> = emptyList(),
    val effectiveDate: String = "",
    val version: String = "",
    val showAcceptButton: Boolean = false,
    val hasAcceptedTerms: Boolean = false,
    val error: String? = null
)

data class TermsSection(
    val title: String,
    val content: String
)