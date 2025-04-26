package com.teksxt.closedtesting.chat.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.teksxt.closedtesting.chat.domain.model.ChatMessage
import com.teksxt.closedtesting.chat.domain.model.MessageType
import com.teksxt.closedtesting.chat.domain.repository.ChatRepository
import com.teksxt.closedtesting.core.util.Resource
import com.teksxt.closedtesting.myrequest.domain.model.AssignedTester
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import com.teksxt.closedtesting.settings.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val requestRepository: RequestRepository,
    savedStateHandle: SavedStateHandle,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])
    private val testerId: String = checkNotNull(savedStateHandle["testerId"])
    private val dayNumber: Int? = (savedStateHandle["dayNumber"] as? String)?.toIntOrNull()

    val currentUserId: String = auth.currentUser?.uid ?: ""

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _testerInfo = MutableStateFlow<AssignedTester?>(null)
    val testerInfo: StateFlow<AssignedTester?> = _testerInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()


    init {
        loadTesterInfo()
        loadMessages()
    }

    private fun loadTesterInfo() {
        viewModelScope.launch {
            try {
                requestRepository.getAssignedTesters(requestId).onSuccess {

                    _testerInfo.value = it.values.flatten().find { tester -> tester.id == testerId }

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
                val result = chatRepository.sendImageMessage(
                    requestId = requestId,
                    senderId = currentUserId,
                    receiverId = receiverId,
                    dayNumber = dayNumber,
                    imageUri = imageUri
                )

                if (result.isFailure) {
                    _errorMessage.emit("Failed to send image")
                }
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to send image")
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

    private fun getChatId(requestId: String, userId1: String, userId2: String): String
    {
        val userIds = listOf(userId1, userId2).sorted().joinToString("_")
        return "${requestId}_${userIds}"
    }

    fun updateTypingStatus(isTyping: Boolean) {
        viewModelScope.launch {
            _isTyping.value = isTyping

            try {
                // Generate chat ID consistently
                val chatId = getChatId(requestId, currentUserId, testerId)

                // Update typing status in Firebase
                chatRepository.setUserTypingStatus(chatId, currentUserId, isTyping)
            } catch (e: Exception) {
                // Silently fail for typing status
            }
        }
    }
}