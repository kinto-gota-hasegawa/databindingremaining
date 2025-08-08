package com.example.databindingremaining

import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiService {
    
    private val baseUrl = Constants.API_BASE_URL
    private val timeout = Constants.API_TIMEOUT
    
    suspend fun getUsers(): List<User> {
        delay(500)
        
        return try {
            val url = URL("$baseUrl/users")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = timeout
                readTimeout = timeout
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseUsersFromJson(response)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("API Error: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun getUserById(userId: String): User? {
        delay(300)
        
        return try {
            val url = URL("$baseUrl/users/$userId")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = timeout
                readTimeout = timeout
            }
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseUserFromJson(response)
            } else {
                null
            }
        } catch (e: Exception) {
            println("API Error: ${e.message}")
            null
        }
    }
    
    suspend fun postUser(user: User): Boolean {
        delay(400)
        
        return try {
            val url = URL("$baseUrl/users")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = timeout
                readTimeout = timeout
                setRequestProperty("Content-Type", "application/json")
            }
            
            val jsonBody = userToJson(user)
            connection.outputStream.use { it.write(jsonBody.toByteArray()) }
            
            connection.responseCode == HttpURLConnection.HTTP_CREATED
        } catch (e: Exception) {
            println("API Error: ${e.message}")
            false
        }
    }
    
    suspend fun updateUser(user: User): Boolean {
        delay(400)
        
        return try {
            val url = URL("$baseUrl/users/${user.id}")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = timeout
                readTimeout = timeout
                setRequestProperty("Content-Type", "application/json")
            }
            
            val jsonBody = userToJson(user)
            connection.outputStream.use { it.write(jsonBody.toByteArray()) }
            
            connection.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            println("API Error: ${e.message}")
            false
        }
    }
    
    suspend fun deleteUser(userId: String): Boolean {
        delay(300)
        
        return try {
            val url = URL("$baseUrl/users/$userId")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "DELETE"
                connectTimeout = timeout
                readTimeout = timeout
            }
            
            connection.responseCode == HttpURLConnection.HTTP_NO_CONTENT
        } catch (e: Exception) {
            println("API Error: ${e.message}")
            false
        }
    }
    
    private fun parseUsersFromJson(json: String): List<User> {
        return try {
            val jsonArray = JSONArray(json)
            val users = mutableListOf<User>()
            
            for (i in 0 until jsonArray.length()) {
                val userJson = jsonArray.getJSONObject(i)
                parseUserFromJsonObject(userJson)?.let { users.add(it) }
            }
            
            users
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseUserFromJson(json: String): User? {
        return try {
            val jsonObject = JSONObject(json)
            parseUserFromJsonObject(jsonObject)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseUserFromJsonObject(jsonObject: JSONObject): User? {
        return try {
            User(
                id = jsonObject.getString("id"),
                name = jsonObject.getString("name"),
                email = jsonObject.getString("email"),
                age = jsonObject.getInt("age"),
                profileImageUrl = jsonObject.optString("profileImageUrl", null),
                isActive = jsonObject.optBoolean("isActive", true),
                createdAt = jsonObject.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = jsonObject.optLong("updatedAt", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun userToJson(user: User): String {
        val jsonObject = JSONObject().apply {
            put("id", user.id)
            put("name", user.name)
            put("email", user.email)
            put("age", user.age)
            user.profileImageUrl?.let { put("profileImageUrl", it) }
            put("isActive", user.isActive)
            put("createdAt", user.createdAt)
            put("updatedAt", user.updatedAt)
        }
        return jsonObject.toString()
    }
}