package com.teksxt.closedtesting.presentation.help

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.data.preferences.UserPreferences
import com.teksxt.closedtesting.domain.usecase.help.SubmitContactFormUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpSupportViewModel @Inject constructor(
    private val submitContactFormUseCase: SubmitContactFormUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HelpSupportUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadFaqs()
        loadGuides()

        // Filter FAQs whenever search query changes
        searchQuery
            .debounce(300)
            .onEach { query ->
                if (query.isEmpty()) {
                    loadFaqs()
                } else {
                    filterFaqs(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun selectTab(tab: HelpSupportTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleFaqExpansion(id: String) {
        val updatedFaqs = _uiState.value.faqItems.map { faqItem ->
            if (faqItem.id == id) {
                faqItem.copy(isExpanded = !faqItem.isExpanded)
            } else {
                faqItem
            }
        }
        _uiState.update { it.copy(faqItems = updatedFaqs) }
    }

    fun updateContactName(name: String) {
        _uiState.update { it.copy(contactName = name) }
    }

    fun updateContactEmail(email: String) {
        _uiState.update { it.copy(contactEmail = email) }
    }

    fun updateContactSubject(subject: String) {
        _uiState.update { it.copy(contactSubject = subject) }
    }

    fun updateContactMessage(message: String) {
        _uiState.update { it.copy(contactMessage = message) }
    }

    fun submitContactForm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }

            try {
                val result = submitContactFormUseCase(
                    name = uiState.value.contactName,
                    email = uiState.value.contactEmail,
                    subject = uiState.value.contactSubject,
                    message = uiState.value.contactMessage,
                    userId = userPreferences.getUserId() ?: ""
                )

                // TODO check this
//                if (result) {
//                    _uiState.update { it.copy(
//                        isSubmitting = false,
//                        successMessage = "Your message has been sent successfully!",
//                        contactName = "",
//                        contactEmail = "",
//                        contactSubject = "",
//                        contactMessage = ""
//                    )}
//                } else {
//                    _uiState.update { it.copy(
//                        isSubmitting = false,
//                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to send message"
//                    )}
//                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "An unknown error occurred"
                )}
            }
        }
    }

    fun selectGuide(id: String) {
        // Implementation for opening guides would go here
        // For now, just print to console
        println("Selected guide: $id")
    }

    private fun loadFaqs() {
        // In a real app, this would fetch from a repository
        val faqs = listOf(
            FAQItem(
                id = "faq1",
                question = "How do I become an app tester?",
                answer = "To become an app tester on TestSync, simply create an account, complete your profile with your testing preferences and devices, then browse the Explore section to find apps that interest you.",
                isExpanded = false
            ),
            FAQItem(
                id = "faq2",
                question = "How do I submit my app for testing?",
                answer = "To submit your app for testing, navigate to the 'My Requests' section and click on 'Create New Request'. Fill in the details about your app, testing requirements, and upload any necessary files or links.",
                isExpanded = false
            ),
            FAQItem(
                id = "faq3",
                question = "How is feedback collected?",
                answer = "Feedback is collected through structured daily testing tasks. Testers complete assigned tasks each day and submit screenshots along with detailed comments about their experience. All feedback is organized by date and feature for easy review.",
                isExpanded = false
            ),
            FAQItem(
                id = "faq4",
                question = "What devices can I specify for testing?",
                answer = "You can specify Android devices with different OS versions, screen sizes, and manufacturers. You can also request specific device features like NFC, fingerprint sensors, or foldable screens depending on your app's requirements.",
                isExpanded = false
            ),
            FAQItem(
                id = "faq5",
                question = "How do I track testing progress?",
                answer = "You can track testing progress in real-time through the Request Details screen. This shows a daily breakdown of testing activities, completion rates, and all submitted feedback and screenshots.",
                isExpanded = false
            )
        )

        _uiState.update { it.copy(faqItems = faqs, allFaqItems = faqs) }
    }

    private fun loadGuides() {
        // In a real app, this would fetch from a repository
        val guides = listOf(
            GuideItem(
                id = "guide1",
                title = "Getting Started with TestSync",
                description = "Learn the basics of using the platform",
                icon = Icons.Outlined.Start
            ),
            GuideItem(
                id = "guide2",
                title = "Creating Effective Test Requests",
                description = "Best practices for app submissions",
                icon = Icons.Outlined.Edit
            ),
            GuideItem(
                id = "guide3",
                title = "Providing Quality Feedback",
                description = "How to write helpful test reports",
                icon = Icons.Outlined.RateReview
            ),
            GuideItem(
                id = "guide4",
                title = "Understanding Test Analytics",
                description = "Making sense of your testing data",
                icon = Icons.Outlined.Assessment
            )
        )

        _uiState.update { it.copy(guides = guides) }
    }

    private fun filterFaqs(query: String) {
        val filteredFaqs = _uiState.value.allFaqItems.filter { faq ->
            faq.question.contains(query, ignoreCase = true) ||
                    faq.answer.contains(query, ignoreCase = true)
        }

        _uiState.update { it.copy(faqItems = filteredFaqs) }
    }
}

data class HelpSupportUiState(
    val selectedTab: HelpSupportTab = HelpSupportTab.FAQ,
    val faqItems: List<FAQItem> = emptyList(),
    val allFaqItems: List<FAQItem> = emptyList(),
    val guides: List<GuideItem> = emptyList(),
    val contactName: String = "",
    val contactEmail: String = "",
    val contactSubject: String = "",
    val contactMessage: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

enum class HelpSupportTab(val title: String) {
    FAQ("FAQs"),
    CONTACT("Contact"),
    GUIDES("Guides")
}

data class FAQItem(
    val id: String,
    val question: String,
    val answer: String,
    val isExpanded: Boolean
)

data class GuideItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)