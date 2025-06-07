package com.teksxt.closedtesting.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.core.util.Resource
import com.teksxt.closedtesting.domain.model.Notification
import com.teksxt.closedtesting.domain.model.NotificationType
import com.teksxt.closedtesting.notifications.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsState(
    val notifications: List<Notification> = emptyList(),
    val filteredNotifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val unreadCounts: Map<NotificationType, Int> = emptyMap()
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    private var currentFilter = NotificationType.ALL

    val unreadCount: StateFlow<Int> = notificationRepository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    init {
        loadNotifications()
    }

    fun refreshNotifications() {
        loadNotifications(forceRefresh = true)
    }

    fun filterNotifications(type: NotificationType) {
        currentFilter = type

        val filtered = if (type == NotificationType.ALL) {
            _state.value.notifications
        } else {
            _state.value.notifications.filter { notification ->
                notification.type == type.displayName.lowercase()
            }
        }

        _state.update {
            it.copy(filteredNotifications = filtered)
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)

            // Update local state
            val updatedNotifications = _state.value.notifications.map { notification ->
                if (notification.id == notificationId) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            }

            updateNotificationsState(updatedNotifications)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()

            // Update local state
            val updatedNotifications = _state.value.notifications.map { notification ->
                notification.copy(isRead = true)
            }

            updateNotificationsState(updatedNotifications)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)

            // Update local state
            val updatedNotifications = _state.value.notifications.filter {
                it.id != notificationId
            }

            updateNotificationsState(updatedNotifications)
        }
    }

    suspend fun clearAllNotifications(): Boolean {
        return try {
            notificationRepository.clearAllNotifications()

            // Update local state
            updateNotificationsState(emptyList())
            true
        } catch (e: Exception) {
            _state.update {
                it.copy(error = "Failed to clear notifications: ${e.message}")
            }
            false
        }
    }

    fun clearError() {
        _state.update {
            it.copy(error = null)
        }
    }

    private fun loadNotifications(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, error = null)
            }

            notificationRepository.getNotifications(forceRefresh).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        updateNotificationsState(result.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Unknown error occurred"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _state.update {
                            it.copy(isLoading = true)
                        }
                    }
                }
            }
        }
    }

    private fun updateNotificationsState(notifications: List<Notification>) {
        // Calculate unread counts
        val unreadCounts = mutableMapOf<NotificationType, Int>()

        // Count for ALL
        unreadCounts[NotificationType.ALL] = notifications.count { !it.isRead }

        // Count by types
        NotificationType.values().filter { it != NotificationType.ALL }.forEach { type ->
            val count = notifications.count {
                !it.isRead && it.type == type.name.lowercase()
            }
            unreadCounts[type] = count
        }

        // Apply current filter
        val filtered = if (currentFilter == NotificationType.ALL) {
            notifications
        } else {
            notifications.filter { it.type == currentFilter.name.lowercase() }
        }

        _state.update {
            it.copy(
                notifications = notifications,
                filteredNotifications = filtered,
                isLoading = false,
                unreadCounts = unreadCounts
            )
        }
    }
}