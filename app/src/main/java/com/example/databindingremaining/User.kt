package com.example.databindingremaining

import java.io.Serializable

data class User(
    val id: String,
    val name: String,
    val email: String,
    val age: Int,
    val profileImageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    
    fun getDisplayName(): String {
        return if (name.isNotBlank()) name else "Anonymous User"
    }
    
    fun isValidEmail(): Boolean {
        return email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
    }
    
    fun isAdult(): Boolean {
        return age >= 18
    }
    
    fun getInitials(): String {
        return name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
    }
    
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "age" to age,
            "profileImageUrl" to profileImageUrl,
            "isActive" to isActive,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                email = map["email"] as? String ?: "",
                age = (map["age"] as? Number)?.toInt() ?: 0,
                profileImageUrl = map["profileImageUrl"] as? String,
                isActive = map["isActive"] as? Boolean ?: true,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
        
        fun createEmpty(): User {
            return User(
                id = "",
                name = "",
                email = "",
                age = 0
            )
        }
    }
}