package com.example.databindingremaining

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class UserRepository {
    
    private val apiService = ApiService()
    private val databaseHelper = DatabaseHelper()
    private val userCache = mutableListOf<User>()
    
    suspend fun fetchUsers(): List<User> = withContext(Dispatchers.IO) {
        try {
            val remoteUsers = apiService.getUsers()
            if (remoteUsers.isNotEmpty()) {
                userCache.clear()
                userCache.addAll(remoteUsers)
                databaseHelper.saveUsers(remoteUsers)
                return@withContext remoteUsers
            }
        } catch (e: Exception) {
            println("Failed to fetch from API: ${e.message}")
        }
        
        val localUsers = databaseHelper.getAllUsers()
        if (localUsers.isNotEmpty()) {
            userCache.clear()
            userCache.addAll(localUsers)
            return@withContext localUsers
        }
        
        return@withContext generateMockUsers()
    }
    
    suspend fun saveUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            val success = databaseHelper.insertUser(user)
            if (success) {
                userCache.add(user)
                apiService.postUser(user)
            }
            success
        } catch (e: Exception) {
            println("Failed to save user: ${e.message}")
            false
        }
    }
    
    suspend fun deleteUser(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val success = databaseHelper.deleteUser(userId)
            if (success) {
                userCache.removeAll { it.id == userId }
                apiService.deleteUser(userId)
            }
            success
        } catch (e: Exception) {
            println("Failed to delete user: ${e.message}")
            false
        }
    }
    
    suspend fun updateUser(user: User): Boolean = withContext(Dispatchers.IO) {
        try {
            val success = databaseHelper.updateUser(user)
            if (success) {
                val index = userCache.indexOfFirst { it.id == user.id }
                if (index != -1) {
                    userCache[index] = user
                }
                apiService.updateUser(user)
            }
            success
        } catch (e: Exception) {
            println("Failed to update user: ${e.message}")
            false
        }
    }
    
    suspend fun getUserById(userId: String): User? = withContext(Dispatchers.IO) {
        userCache.find { it.id == userId }
            ?: databaseHelper.getUserById(userId)
            ?: apiService.getUserById(userId)
    }
    
    private fun generateMockUsers(): List<User> {
        return listOf(
            User(UUID.randomUUID().toString(), "John Doe", "john@example.com", 25),
            User(UUID.randomUUID().toString(), "Jane Smith", "jane@example.com", 30),
            User(UUID.randomUUID().toString(), "Bob Johnson", "bob@example.com", 28),
            User(UUID.randomUUID().toString(), "Alice Brown", "alice@example.com", 32),
            User(UUID.randomUUID().toString(), "Charlie Wilson", "charlie@example.com", 27)
        )
    }
    
    fun clearCache() {
        userCache.clear()
    }
}