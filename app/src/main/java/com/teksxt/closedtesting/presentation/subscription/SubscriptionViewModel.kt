package com.teksxt.closedtesting.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teksxt.closedtesting.domain.model.Subscription
import com.teksxt.closedtesting.domain.usecase.auth.GetCurrentUserUseCase
import com.teksxt.closedtesting.domain.usecase.subscription.GetSubscriptionStatusUseCase
import com.teksxt.closedtesting.domain.usecase.subscription.PurchaseSubscriptionUseCase
import com.teksxt.closedtesting.domain.usecase.subscription.ValidateSubscriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val subscription: Subscription? = null,
    val error: String? = null,
    val isPurchasing: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val currentUserId: String? = null,
    val isSubscriptionValid: Boolean = false
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSubscriptionStatusUseCase: GetSubscriptionStatusUseCase,
    private val purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase,
    private val validateSubscriptionUseCase: ValidateSubscriptionUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase()
                user?.let {
                    _uiState.update { state ->
                        state.copy(currentUserId = it.id)
                    }
                    getSubscriptionStatus(it.id)
                    validateSubscription(it.id)
                }
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(error = exception.message ?: "Error loading user")
                }
            }
        }
    }

    fun getSubscriptionStatus(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getSubscriptionStatusUseCase(userId).collect { subscription ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            subscription = subscription,
                            error = null
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error fetching subscription status"
                    )
                }
            }
        }
    }

    fun purchaseSubscription(planType: String, paymentMethod: String) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUserId ?: return@launch
            
            _uiState.update { 
                it.copy(
                    isPurchasing = true,
                    error = null
                )
            }
            
            try {
                val success = purchaseSubscriptionUseCase(userId, planType, paymentMethod)
                _uiState.update { 
                    it.copy(
                        isPurchasing = false,
                        purchaseSuccess = success,
                        error = null
                    )
                }
                if (success) {
                    getSubscriptionStatus(userId)
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isPurchasing = false,
                        purchaseSuccess = false,
                        error = exception.message ?: "Error purchasing subscription"
                    )
                }
            }
        }
    }

    fun validateSubscription(userId: String) {
        viewModelScope.launch {
            try {
                val isValid = validateSubscriptionUseCase(userId)
                _uiState.update { it.copy(isSubscriptionValid = isValid) }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isSubscriptionValid = false,
                        error = exception.message ?: "Error validating subscription"
                    )
                }
            }
        }
    }

    fun resetPurchaseState() {
        _uiState.update { it.copy(purchaseSuccess = false, error = null) }
    }

    fun clearErrors() {
        _uiState.update { it.copy(error = null) }
    }
}