package com.teksxt.closedtesting.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        
    // Cache for current preferences
    private val _userIdFlow = MutableStateFlow(getUserId())
    val userIdFlow: Flow<String?> = _userIdFlow.asStateFlow()
    
    private val _userRoleFlow = MutableStateFlow(getUserRole())
    val userRoleFlow: Flow<String?> = _userRoleFlow.asStateFlow()
    
    private val _isOnboardedFlow = MutableStateFlow(isUserOnboarded())
    val isOnboardedFlow: Flow<Boolean> = _isOnboardedFlow.asStateFlow()
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_IS_ONBOARDED = "is_onboarded"
        private const val KEY_LAST_SYNC = "last_sync"
    }
    
    fun saveUserId(userId: String) {
        sharedPreferences.edit {
            putString(KEY_USER_ID, userId)
        }
        _userIdFlow.value = userId
    }
    
    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }
    
    fun clearUserId() {
        sharedPreferences.edit {
            remove(KEY_USER_ID)
        }
        _userIdFlow.value = null
    }
    
    fun saveUserRole(role: String) {
        sharedPreferences.edit {
            putString(KEY_USER_ROLE, role)
        }
        _userRoleFlow.value = role
    }
    
    fun getUserRole(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }
    
    fun saveUserOnboardingStatus(isOnboarded: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_IS_ONBOARDED, isOnboarded)
        }
        _isOnboardedFlow.value = isOnboarded
    }
    
    fun isUserOnboarded(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_ONBOARDED, false)
    }
    
    fun saveLastSyncTime(timestamp: Long) {
        sharedPreferences.edit {
            putLong(KEY_LAST_SYNC, timestamp)
        }
    }
    
    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0)
    }
    
    fun clearAll() {
        sharedPreferences.edit {
            clear()
        }
        _userIdFlow.value = null
        _userRoleFlow.value = null
        _isOnboardedFlow.value = false
    }
}