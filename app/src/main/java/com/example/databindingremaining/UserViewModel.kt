package com.example.databindingremaining

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    
    private val repository = UserRepository()
    
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        loadUsers()
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                delay(1000)
                val userList = repository.fetchUsers()
                _users.value = userList
            } catch (e: Exception) {
                _error.value = e.message
                _users.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshData() {
        if (_isLoading.value != true) {
            loadUsers()
        }
    }
    
    fun addUser(user: User) {
        viewModelScope.launch {
            try {
                repository.saveUser(user)
                val currentList = _users.value.orEmpty()
                _users.value = currentList + user
            } catch (e: Exception) {
                _error.value = "Failed to add user: ${e.message}"
            }
        }
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteUser(userId)
                val currentList = _users.value.orEmpty()
                _users.value = currentList.filter { it.id != userId }
            } catch (e: Exception) {
                _error.value = "Failed to delete user: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}