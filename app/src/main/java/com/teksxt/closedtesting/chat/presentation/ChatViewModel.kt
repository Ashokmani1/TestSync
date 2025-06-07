package com.teksxt.closedtesting.chat.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teksxt.closedtesting.TestSyncApp
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.chat.domain.model.MessageType
import com.teksxt.closedtesting.chat.domain.repository.ChatRepository
import com.teksxt.closedtesting.core.util.FileUtil
import com.teksxt.closedtesting.core.util.Resource
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.settings.domain.model.User
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import com.teksxt.closedtesting.ui.theme.TestSyncTheme
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])
    private val testerId: String = checkNotNull(savedStateHandle["testerId"])

    val currentUserId: String = auth.currentUser?.uid ?: ""

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _testerInfo = MutableStateFlow<User?>(null)
    val testerInfo: StateFlow<User?> = _testerInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _imageSendingState = MutableStateFlow<ImageSendingState>(ImageSendingState.Initial)
    val imageSendingState: StateFlow<ImageSendingState> = _imageSendingState



    init {
        loadTesterInfo()
        loadMessages()
    }

    private fun loadTesterInfo()
    {
        viewModelScope.launch {
            try {
                userRepository.getUserById(testerId).onSuccess { user ->

                    _testerInfo.value = user
                }
            } catch (e: Exception) {
                _errorMessage.emit("Failed to load tester information")
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                chatRepository.getMessagesForRequestAndUsers(
                    requestId = requestId,
                    userId1 = currentUserId,
                    userId2 = testerId
                ).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _messages.value = resource.data ?: emptyList()
                            markMessagesAsRead(resource.data?.filter {
                                !it.isRead && it.receiverId == currentUserId
                            }?.map { it.id } ?: emptyList())
                        }
                        is Resource.Error -> {
                            _errorMessage.emit(resource.message ?: "Failed to load messages")
                        }
                        is Resource.Loading -> {
                            _isLoading.value = true
                        }
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to load messages")
                _isLoading.value = false
            }
        }
    }

    fun sendTextMessage(receiverId: String, dayNumber: Int?, content: String) {
        viewModelScope.launch {
            try {
                val message = ChatMessage(
                    requestId = requestId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    dayNumber = dayNumber,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    messageType = MessageType.TEXT
                )

                val result = chatRepository.sendMessage(message)
                if (result.isFailure) {
                    _errorMessage.emit("Failed to send message")
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to send message")
            }
        }
    }

    fun sendImageMessage(receiverId: String, dayNumber: Int?, imageUri: Uri) {
        viewModelScope.launch {
            try {

                val compressedUri = FileUtil.compressImage(imageUri, TestSyncApp.instance)

                if (compressedUri == null)
                {
                    _errorMessage.emit("Failed to compress image")
                    return@launch
                }

                _imageSendingState.value = ImageSendingState.Uploading
                chatRepository.sendImageMessage(
                    requestId = requestId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    dayNumber = dayNumber,
                    imageUri = compressedUri
                ).onSuccess {
                    _imageSendingState.value = ImageSendingState.Success
                }.onFailure { e ->
                    _imageSendingState.value = ImageSendingState.Error(e.message ?: "Failed to upload image")
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to send image")
                _imageSendingState.value = ImageSendingState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun sendReminder(testerId: String, dayNumber: Int?) {
        viewModelScope.launch {
            try {
                val result = chatRepository.sendReminderMessage(
                    requestId = requestId,
                    dayNumber = dayNumber,
                    testerId = testerId
                )

                if (result.isFailure) {
                    _errorMessage.emit("Failed to send reminder")
                } else {
                    _errorMessage.emit("Reminder sent successfully")
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to send reminder")
            }
        }
    }

    private fun markMessagesAsRead(messageIds: List<String>) {
        if (messageIds.isEmpty()) return

        viewModelScope.launch {
            try {
                chatRepository.markMessagesAsRead(messageIds)
            } catch (e: Exception) {
                // Silent failure is acceptable for message read status
            }
        }
    }


    fun deleteMessage(message: ChatMessage) {
        viewModelScope.launch {
            try {
                if (message.senderId != currentUserId) {
                    _errorMessage.emit("You can only delete your own messages")
                    return@launch
                }

                _isLoading.value = true

                // Pre-remove message from local list for immediate UI feedback
                val currentMessages = _messages.value.toMutableList()
                val messageIndex = currentMessages.indexOfFirst { it.id == message.id }

                if (messageIndex != -1) {
                    currentMessages.removeAt(messageIndex)
                    _messages.value = currentMessages
                }

                val result = chatRepository.deleteMessage(message.id)

                if (result.isFailure) {
                    // If deletion fails, restore the message in the UI
                    if (messageIndex != -1) {
                        _messages.value = _messages.value.toMutableList().apply {
                            add(messageIndex, message)
                        }
                    }

                    _errorMessage.emit("Failed to delete message: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
                }

                delay(500) // Small delay to ensure Firestore has processed the deletion
                refreshMessages()

            } catch (e: Exception) {
                _errorMessage.emit("Error: ${e.message ?: "Unknown error"}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun refreshMessages() {
        viewModelScope.launch {
            try {
                chatRepository.getMessagesForRequestAndUsers(
                    requestId = requestId,
                    userId1 = currentUserId,
                    userId2 = testerId
                ).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _messages.value = resource.data ?: emptyList()
                        }
                        is Resource.Error -> {
                            _errorMessage.emit(resource.message ?: "Failed to refresh messages")
                        }
                        is Resource.Loading -> {
                            // Already handling loading state
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.emit("Failed to refresh: ${e.message}")
            }
        }
    }

    fun shareApp(iconUri: Uri, appName: String, appDescription: String): Result<String> {
//        if (appName.isBlank() || appDescription.isBlank()) {
//            return Result.failure(IllegalArgumentException("App name and description are required"))
//        }
//
//        viewModelScope.launch {
//            try {
//                _isLoading.value = true
//
//                // Upload icon to storage
//                val imageRef = storage.reference.child("app_icons/${UUID.randomUUID()}.jpg")
//                val uploadTask = imageRef.putFile(iconUri)
//                val downloadUrl = uploadTask.continueWithTask { task ->
//                    if (!task.isSuccessful) {
//                        task.exception?.let { throw it }
//                    }
//                    imageRef.downloadUrl
//                }.await()
//
//                // Create app object
//                val appId = UUID.randomUUID().toString()
//                val currentTime = System.currentTimeMillis()
//                val app = hashMapOf(
//                    "id" to appId,
//                    "name" to appName,
//                    "description" to appDescription,
//                    "iconUrl" to downloadUrl.toString(),
//                    "ownerId" to currentUserId,
//                    "createdAt" to Timestamp(Date(currentTime)),
//                    "updatedAt" to Timestamp(Date(currentTime)),
//                    "status" to "PENDING_REVIEW",
//                    "testingDays" to 7, // Default testing period
//                    "activeTesters" to 0,
//                    "totalTesters" to 0
//                )
//
//                // Save to Firestore
//                firestore.collection("apps")
//                    .document(appId)
//                    .set(app)
//                    .await()
//
//                _isLoading.value = false
//                return@launch Result.success(appId)
//            } catch (e: Exception) {
//                _isLoading.value = false
//                return@launch Result.failure(e)
//            }
//        }
//
//        return Result.success("App submitted")

        return Result.success("")
    }
}

sealed class ImagePickerState {
    object Initial : ImagePickerState()
    object Uploading : ImagePickerState()
    data class Error(val message: String) : ImagePickerState()
}

sealed class ImageSendingState {
    object Initial : ImageSendingState()
    object Uploading : ImageSendingState()
    object Success : ImageSendingState()
    data class Error(val message: String) : ImageSendingState()
}