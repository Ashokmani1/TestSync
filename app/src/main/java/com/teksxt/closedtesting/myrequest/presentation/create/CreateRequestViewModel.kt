package com.teksxt.closedtesting.myrequest.presentation.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CreateRequestViewModel @Inject constructor(
    private val requestRepository: RequestRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Form fields
    var appName by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var groupLink by mutableStateOf("")
        private set
    var playStoreLink by mutableStateOf("")
        private set
    var numberOfTesters by mutableStateOf("")
        private set
    var durationInDays by mutableStateOf("")
        private set
    var isPremium by mutableStateOf(false)
        private set

    // Form field errors
    var appNameError by mutableStateOf<String?>(null)
        private set
    var descriptionError by mutableStateOf<String?>(null)
        private set
    var groupLinkError by mutableStateOf<String?>(null)
        private set
    var playStoreLinkError by mutableStateOf<String?>(null)
        private set
    var numberOfTestersError by mutableStateOf<String?>(null)
        private set
    var durationInDaysError by mutableStateOf<String?>(null)
        private set

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Success event
    private val _createSuccess = MutableSharedFlow<String>() // String for the created request ID
    val createSuccess: SharedFlow<String> = _createSuccess.asSharedFlow()

    // Error event
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    // Update form fields
    fun updateAppName(value: String) {
        appName = value
        appNameError = null
    }

    fun updateDescription(value: String) {
        description = value
        descriptionError = null
    }

    fun updateGroupLink(value: String) {
        groupLink = value
        groupLinkError = null
    }

    fun updatePlayStoreLink(value: String) {
        playStoreLink = value
        playStoreLinkError = null
    }

    fun updateNumberOfTesters(value: String) {
        numberOfTesters = value
        numberOfTestersError = null
    }

    fun updateDurationInDays(value: String) {
        durationInDays = value
        durationInDaysError = null
    }

    fun updateIsPremium(value: Boolean) {
        isPremium = value
    }

    // Validation
    private fun validateForm(): Boolean {
        var isValid = true

        if (appName.isBlank()) {
            appNameError = "App name cannot be empty"
            isValid = false
        }

        if (description.isBlank()) {
            descriptionError = "Description cannot be empty"
            isValid = false
        }

        if (groupLink.isBlank()) {
            groupLinkError = "Google Group link cannot be empty"
            isValid = false
        } else if (!isValidUrl(groupLink)) {
            groupLinkError = "Enter a valid URL"
            isValid = false
        }

        if (playStoreLink.isBlank()) {
            playStoreLinkError = "Play Store link cannot be empty"
            isValid = false
        } else if (!isValidUrl(playStoreLink)) {
            playStoreLinkError = "Enter a valid URL"
            isValid = false
        }

        if (numberOfTesters.isBlank()) {
            numberOfTestersError = "Number of testers cannot be empty"
            isValid = false
        } else {
            try {
                val numTesters = numberOfTesters.toInt()
                if (numTesters <= 0) {
                    numberOfTestersError = "Number of testers must be positive"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                numberOfTestersError = "Enter a valid number"
                isValid = false
            }
        }

        if (durationInDays.isBlank()) {
            durationInDaysError = "Duration cannot be empty"
            isValid = false
        } else {
            try {
                val days = durationInDays.toInt()
                if (days <= 0) {
                    durationInDaysError = "Duration must be positive"
                    isValid = false
                } else if (days > 28) {
                    durationInDaysError = "Maximum duration is 28 days"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                durationInDaysError = "Enter a valid number"
                isValid = false
            }
        }

        return isValid
    }

    // Simple URL validation
    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    // Submit the form
    fun submitRequest() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val currentUserId = auth.currentUser?.uid
                if (currentUserId == null) {
                    _errorMessage.emit("User not authenticated")
                    return@launch
                }
                
                // Generate app ID from Play Store link
                val appId = com.teksxt.closedtesting.util.TestSyncUtil.generateAppId(playStoreLink)

                val request = Request(
                    appId = appId, // Use generated appId
                    appName = appName,
                    description = description,
                    groupLink = groupLink,
                    playStoreLink = playStoreLink,
                    numberOfTesters = numberOfTesters.toInt(),
                    durationInDays = durationInDays.toInt(),
                    isPremium = isPremium,
                    createdBy = currentUserId,
                    createdAt = Date(),
                    status = "active"
                )

                val requestId = requestRepository.createRequest(request)
                _createSuccess.emit(requestId)

            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to create request")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetErrors() {
        appNameError = null
        descriptionError = null
        groupLinkError = null
        playStoreLinkError = null
        numberOfTestersError = null
        durationInDaysError = null
    }
}