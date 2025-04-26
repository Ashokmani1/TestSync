package com.teksxt.closedtesting.presentation.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TermsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TermsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTerms()
    }

    fun acceptTerms() {
        viewModelScope.launch {
            // In a real app, this would call a use case to record the user's acceptance
            // For now, just print to console
            println("Terms accepted by user")
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

data class TermsUiState(
    val isLoading: Boolean = false,
    val sections: List<TermsSection> = emptyList(),
    val effectiveDate: String = "",
    val version: String = "",
    val showAcceptButton: Boolean = false
)

data class TermsSection(
    val title: String,
    val content: String
)