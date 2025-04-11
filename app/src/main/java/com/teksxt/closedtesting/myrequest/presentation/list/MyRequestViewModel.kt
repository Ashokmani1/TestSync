package com.teksxt.closedtesting.myrequest.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.myrequest.domain.model.Request
import com.teksxt.closedtesting.myrequest.domain.repo.RequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRequestViewModel @Inject constructor(
    private val requestRepository: RequestRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    // Get requests created by the current user
    val requests: StateFlow<List<Request>> = requestRepository.getUserRequests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshRequests()
    }

    fun refreshRequests() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                requestRepository.syncRequests()
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to refresh requests")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRequest(request: Request) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                requestRepository.deleteRequest(request)
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Failed to delete request")
            } finally {
                _isLoading.value = false
            }
        }
    }
}