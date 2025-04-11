package com.teksxt.closedtesting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.profile.domain.model.User
import com.teksxt.closedtesting.domain.usecase.auth.GetCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {
    private val _userSession = MutableStateFlow<User?>(null)
    val userSession: StateFlow<User?> = _userSession.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = getCurrentUserUseCase()
                _userSession.value = user
            } catch (e: Exception) {
                _userSession.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}