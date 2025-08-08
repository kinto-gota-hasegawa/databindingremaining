package com.example.databindingremaining

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class HogeUiState(
    val message: String = "Hello Hoge!",
    val counter: Int = 0,
    val isLoading: Boolean = false,
    val items: List<String> = emptyList()
)

class HogeViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(HogeUiState())
    val uiState: StateFlow<HogeUiState> = _uiState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    fun incrementCounter() {
        _uiState.value = _uiState.value.copy(
            counter = _uiState.value.counter + 1
        )
    }
    
    fun updateMessage(newMessage: String) {
        _uiState.value = _uiState.value.copy(
            message = newMessage
        )
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulate network call
            delay(2000)
            
            val sampleItems = listOf(
                "Item 1",
                "Item 2", 
                "Item 3",
                "Modern Kotlin Item",
                "Coroutine Item"
            )
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                items = sampleItems
            )
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            delay(1500)
            
            val refreshedItems = _uiState.value.items.map { item ->
                "$item (refreshed)"
            }
            
            _uiState.value = _uiState.value.copy(
                items = refreshedItems
            )
            
            _isRefreshing.value = false
        }
    }
    
    fun reset() {
        _uiState.value = HogeUiState()
        _isRefreshing.value = false
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HogeViewModel() as T
            }
        }
    }
}