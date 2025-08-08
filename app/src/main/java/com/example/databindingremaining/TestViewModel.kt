package com.example.databindingremaining

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.random.Random

class TestViewModel: ViewModel() {
    // State management properties
    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _userList = MutableStateFlow<List<User>>(emptyList())
    val userList: StateFlow<List<User>> = _userList.asStateFlow()
    
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = _filteredUsers.asStateFlow()
    
    private val _counter = MutableStateFlow(0)
    val counter: StateFlow<Int> = _counter.asStateFlow()
    
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    // Repository simulation
    private val repository = TestRepository()
    
    init {
        initializeData()
        startPeriodicUpdates()
    }
    
    /**
     * Initialize default data and setup initial state
     */
    private fun initializeData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                delay(1000) // Simulate loading delay
                
                val initialUsers = generateInitialUsers()
                _userList.value = initialUsers
                _filteredUsers.value = initialUsers
                
                _uiState.value = _uiState.value.copy(
                    isInitialized = true,
                    lastUpdated = System.currentTimeMillis()
                )
                
                generateInitialNotifications()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Generate initial user data for testing
     */
    private fun generateInitialUsers(): List<User> {
        val names = listOf(
            "John Doe", "Jane Smith", "Mike Johnson", "Sarah Wilson", "David Brown",
            "Emma Davis", "Chris Miller", "Lisa Garcia", "Robert Martinez", "Mary Rodriguez",
            "James Lopez", "Patricia Hernandez", "Michael Anderson", "Linda Taylor", "William Thomas",
            "Barbara Jackson", "Richard White", "Elizabeth Harris", "Joseph Martin", "Susan Thompson"
        )
        
        val departments = listOf("Engineering", "Marketing", "Sales", "HR", "Finance", "Operations")
        val roles = listOf("Manager", "Developer", "Analyst", "Specialist", "Coordinator", "Lead")
        
        return names.mapIndexed { index, name ->
            User(
                id = index + 1,
                name = name,
                email = "${name.lowercase().replace(" ", ".")}@company.com",
                department = departments.random(),
                role = roles.random(),
                isActive = Random.nextBoolean(),
                joinDate = System.currentTimeMillis() - Random.nextLong(365 * 24 * 60 * 60 * 1000L),
                salary = Random.nextInt(50000, 150000),
                profileImageUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=${name.replace(" ", "")}",
                phoneNumber = generatePhoneNumber(),
                address = generateAddress(),
                skills = generateSkills(),
                projects = generateProjects(),
                performanceRating = Random.nextDouble(3.0, 5.0)
            )
        }
    }
    
    /**
     * Generate random phone number
     */
    private fun generatePhoneNumber(): String {
        val areaCode = Random.nextInt(200, 999)
        val firstPart = Random.nextInt(100, 999)
        val secondPart = Random.nextInt(1000, 9999)
        return "+1-$areaCode-$firstPart-$secondPart"
    }
    
    /**
     * Generate random address
     */
    private fun generateAddress(): String {
        val streets = listOf("Main St", "Oak Ave", "Pine Rd", "Elm Dr", "Maple Ln", "Cedar Blvd")
        val cities = listOf("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia")
        val states = listOf("NY", "CA", "IL", "TX", "AZ", "PA")
        
        val streetNumber = Random.nextInt(1, 9999)
        val street = streets.random()
        val city = cities.random()
        val state = states.random()
        val zipCode = Random.nextInt(10000, 99999)
        
        return "$streetNumber $street, $city, $state $zipCode"
    }
    
    /**
     * Generate random skills for user
     */
    private fun generateSkills(): List<String> {
        val allSkills = listOf(
            "Kotlin", "Java", "JavaScript", "Python", "React", "Angular", "Vue.js",
            "Node.js", "Spring Boot", "Django", "Flask", "SQL", "MongoDB", "Redis",
            "AWS", "Azure", "Docker", "Kubernetes", "Git", "Jenkins", "Agile", "Scrum"
        )
        return allSkills.shuffled().take(Random.nextInt(3, 8))
    }
    
    /**
     * Generate random projects for user
     */
    private fun generateProjects(): List<String> {
        val projects = listOf(
            "Mobile App Redesign", "Backend API Migration", "Database Optimization",
            "User Authentication System", "Payment Gateway Integration", "Analytics Dashboard",
            "Performance Monitoring", "Security Audit", "Cloud Migration", "DevOps Pipeline"
        )
        return projects.shuffled().take(Random.nextInt(1, 4))
    }
    
    /**
     * Generate initial notifications
     */
    private fun generateInitialNotifications() {
        val notifications = listOf(
            Notification(
                id = 1,
                title = "System Update",
                message = "System will be updated tonight at 2 AM",
                timestamp = System.currentTimeMillis(),
                type = NotificationType.INFO,
                isRead = false
            ),
            Notification(
                id = 2,
                title = "New User Registered",
                message = "John Doe has joined the team",
                timestamp = System.currentTimeMillis() - 3600000,
                type = NotificationType.SUCCESS,
                isRead = false
            ),
            Notification(
                id = 3,
                title = "Server Alert",
                message = "High CPU usage detected on server-01",
                timestamp = System.currentTimeMillis() - 7200000,
                type = NotificationType.WARNING,
                isRead = true
            )
        )
        _notifications.value = notifications
    }
    
    /**
     * Start periodic updates for real-time data
     */
    private fun startPeriodicUpdates() {
        viewModelScope.launch {
            while (true) {
                delay(30000) // Update every 30 seconds
                updateSystemStatus()
                checkForNewNotifications()
            }
        }
    }
    
    /**
     * Update system status periodically
     */
    private suspend fun updateSystemStatus() {
        try {
            val isOnline = Random.nextBoolean() // Simulate network status
            _isOnline.value = isOnline
            
            if (isOnline) {
                // Simulate fetching updated data
                val updatedUsers = _userList.value.map { user ->
                    user.copy(
                        isActive = if (Random.nextDouble() < 0.1) !user.isActive else user.isActive,
                        performanceRating = if (Random.nextDouble() < 0.05) 
                            Random.nextDouble(3.0, 5.0) else user.performanceRating
                    )
                }
                _userList.value = updatedUsers
                filterUsers(_searchQuery.value)
            }
            
            _uiState.value = _uiState.value.copy(
                lastUpdated = System.currentTimeMillis(),
                connectionStatus = if (isOnline) "Connected" else "Offline"
            )
        } catch (e: Exception) {
            _errorMessage.value = "Failed to update system status: ${e.message}"
        }
    }
    
    /**
     * Check for new notifications
     */
    private suspend fun checkForNewNotifications() {
        if (Random.nextDouble() < 0.2) { // 20% chance of new notification
            val newNotification = Notification(
                id = _notifications.value.size + 1,
                title = getRandomNotificationTitle(),
                message = getRandomNotificationMessage(),
                timestamp = System.currentTimeMillis(),
                type = NotificationType.values().random(),
                isRead = false
            )
            
            val updatedNotifications = _notifications.value + newNotification
            _notifications.value = updatedNotifications.takeLast(10) // Keep only last 10
        }
    }
    
    /**
     * Get random notification title
     */
    private fun getRandomNotificationTitle(): String {
        val titles = listOf(
            "System Alert", "New Message", "Task Completed", "Update Available",
            "Security Notice", "Performance Report", "User Activity", "Backup Complete"
        )
        return titles.random()
    }
    
    /**
     * Get random notification message
     */
    private fun getRandomNotificationMessage(): String {
        val messages = listOf(
            "Your request has been processed successfully",
            "New version available for download",
            "Scheduled maintenance completed",
            "Database backup finished",
            "Security scan completed without issues",
            "Monthly report is ready for review",
            "New team member has been added",
            "System performance is optimal"
        )
        return messages.random()
    }
    
    /**
     * Search and filter users based on query
     */
    fun searchUsers(query: String) {
        _searchQuery.value = query
        filterUsers(query)
    }
    
    /**
     * Filter users based on search query
     */
    private fun filterUsers(query: String) {
        val filtered = if (query.isBlank()) {
            _userList.value
        } else {
            _userList.value.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true) ||
                user.department.contains(query, ignoreCase = true) ||
                user.role.contains(query, ignoreCase = true)
            }
        }
        _filteredUsers.value = filtered
    }
    
    /**
     * Select a user
     */
    fun selectUser(user: User) {
        _selectedUser.value = user
        _uiState.value = _uiState.value.copy(selectedUserId = user.id)
    }
    
    /**
     * Clear selected user
     */
    fun clearSelection() {
        _selectedUser.value = null
        _uiState.value = _uiState.value.copy(selectedUserId = null)
    }
    
    /**
     * Add new user
     */
    fun addUser(user: User) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Simulate API call delay
                delay(1000)
                
                val newUser = user.copy(id = _userList.value.size + 1)
                val updatedList = _userList.value + newUser
                _userList.value = updatedList
                
                filterUsers(_searchQuery.value)
                
                addNotification(
                    "User Added", 
                    "Successfully added ${user.name} to the system",
                    NotificationType.SUCCESS
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update existing user
     */
    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Simulate API call delay
                delay(800)
                
                val updatedList = _userList.value.map { user ->
                    if (user.id == updatedUser.id) updatedUser else user
                }
                _userList.value = updatedList
                
                filterUsers(_searchQuery.value)
                
                if (_selectedUser.value?.id == updatedUser.id) {
                    _selectedUser.value = updatedUser
                }
                
                addNotification(
                    "User Updated",
                    "Successfully updated ${updatedUser.name}'s information",
                    NotificationType.INFO
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Delete user
     */
    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Simulate API call delay
                delay(600)
                
                val userToDelete = _userList.value.find { it.id == userId }
                val updatedList = _userList.value.filter { it.id != userId }
                _userList.value = updatedList
                
                filterUsers(_searchQuery.value)
                
                if (_selectedUser.value?.id == userId) {
                    clearSelection()
                }
                
                userToDelete?.let {
                    addNotification(
                        "User Deleted",
                        "Removed ${it.name} from the system",
                        NotificationType.WARNING
                    )
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Increment counter
     */
    fun incrementCounter() {
        _counter.value = _counter.value + 1
    }
    
    /**
     * Decrement counter
     */
    fun decrementCounter() {
        if (_counter.value > 0) {
            _counter.value = _counter.value - 1
        }
    }
    
    /**
     * Reset counter
     */
    fun resetCounter() {
        _counter.value = 0
    }
    
    /**
     * Add notification
     */
    private fun addNotification(title: String, message: String, type: NotificationType) {
        val notification = Notification(
            id = _notifications.value.size + 1,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type,
            isRead = false
        )
        
        val updatedNotifications = _notifications.value + notification
        _notifications.value = updatedNotifications.takeLast(10)
    }
    
    /**
     * Mark notification as read
     */
    fun markNotificationAsRead(notificationId: Int) {
        val updatedNotifications = _notifications.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }
        _notifications.value = updatedNotifications
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Refresh data
     */
    fun refreshData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Simulate refresh delay
                delay(2000)
                
                // Regenerate users with some randomization
                val refreshedUsers = _userList.value.map { user ->
                    user.copy(
                        isActive = Random.nextBoolean(),
                        performanceRating = Random.nextDouble(3.0, 5.0),
                        salary = user.salary + Random.nextInt(-5000, 10000)
                    )
                }
                
                _userList.value = refreshedUsers
                filterUsers(_searchQuery.value)
                
                _uiState.value = _uiState.value.copy(
                    lastUpdated = System.currentTimeMillis(),
                    refreshCount = _uiState.value.refreshCount + 1
                )
                
                addNotification(
                    "Data Refreshed",
                    "All data has been successfully refreshed",
                    NotificationType.SUCCESS
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to refresh data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Export data to different formats
     */
    fun exportData(format: ExportFormat) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Simulate export processing
                delay(3000)
                
                when (format) {
                    ExportFormat.CSV -> exportToCSV()
                    ExportFormat.JSON -> exportToJSON()
                    ExportFormat.PDF -> exportToPDF()
                    ExportFormat.EXCEL -> exportToExcel()
                }
                
                addNotification(
                    "Export Complete",
                    "Data exported successfully to ${format.name} format",
                    NotificationType.SUCCESS
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to export data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Export to CSV format
     */
    private suspend fun exportToCSV() = withContext(Dispatchers.IO) {
        // Simulate CSV export processing
        val csvData = buildString {
            appendLine("ID,Name,Email,Department,Role,Salary,Active,Performance")
            _userList.value.forEach { user ->
                appendLine("${user.id},${user.name},${user.email},${user.department},${user.role},${user.salary},${user.isActive},${user.performanceRating}")
            }
        }
        // In real implementation, would save to file
        println("CSV Export completed: ${csvData.length} characters")
    }
    
    /**
     * Export to JSON format
     */
    private suspend fun exportToJSON() = withContext(Dispatchers.IO) {
        // Simulate JSON export processing
        val jsonData = _userList.value.toString() // Simplified JSON representation
        // In real implementation, would use proper JSON serialization
        println("JSON Export completed: ${jsonData.length} characters")
    }
    
    /**
     * Export to PDF format
     */
    private suspend fun exportToPDF() = withContext(Dispatchers.IO) {
        // Simulate PDF generation
        println("PDF Export completed for ${_userList.value.size} users")
    }
    
    /**
     * Export to Excel format
     */
    private suspend fun exportToExcel() = withContext(Dispatchers.IO) {
        // Simulate Excel generation
        println("Excel Export completed for ${_userList.value.size} users")
    }
    
    /**
     * Import data from external source
     */
    fun importData(source: ImportSource) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Simulate import processing
                delay(5000)
                
                val importedUsers = when (source) {
                    ImportSource.CSV -> importFromCSV()
                    ImportSource.JSON -> importFromJSON()
                    ImportSource.DATABASE -> importFromDatabase()
                    ImportSource.API -> importFromAPI()
                }
                
                _userList.value = _userList.value + importedUsers
                filterUsers(_searchQuery.value)
                
                addNotification(
                    "Import Complete",
                    "Successfully imported ${importedUsers.size} users from ${source.name}",
                    NotificationType.SUCCESS
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to import data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Import from CSV
     */
    private fun importFromCSV(): List<User> {
        // Simulate CSV import
        return generateAdditionalUsers(Random.nextInt(5, 15))
    }
    
    /**
     * Import from JSON
     */
    private fun importFromJSON(): List<User> {
        // Simulate JSON import
        return generateAdditionalUsers(Random.nextInt(3, 10))
    }
    
    /**
     * Import from database
     */
    private fun importFromDatabase(): List<User> {
        // Simulate database import
        return generateAdditionalUsers(Random.nextInt(10, 25))
    }
    
    /**
     * Import from API
     */
    private fun importFromAPI(): List<User> {
        // Simulate API import
        return generateAdditionalUsers(Random.nextInt(8, 20))
    }
    
    /**
     * Generate additional users for import
     */
    private fun generateAdditionalUsers(count: Int): List<User> {
        val additionalNames = listOf(
            "Alex Johnson", "Maria Garcia", "Kevin Chen", "Ashley Brown", "Daniel Kim",
            "Sophia Lee", "Ryan Martinez", "Emily Taylor", "Brandon Wilson", "Olivia Anderson"
        )
        
        return (1..count).map { index ->
            val name = additionalNames.randomOrNull() ?: "User ${_userList.value.size + index}"
            User(
                id = _userList.value.size + index,
                name = name,
                email = "${name.lowercase().replace(" ", ".")}@imported.com",
                department = listOf("Engineering", "Marketing", "Sales", "HR", "Finance").random(),
                role = listOf("Manager", "Developer", "Analyst", "Specialist").random(),
                isActive = Random.nextBoolean(),
                joinDate = System.currentTimeMillis(),
                salary = Random.nextInt(45000, 120000),
                profileImageUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=imported$index",
                phoneNumber = generatePhoneNumber(),
                address = generateAddress(),
                skills = generateSkills(),
                projects = generateProjects(),
                performanceRating = Random.nextDouble(3.0, 5.0)
            )
        }
    }
    
    /**
     * Validate user data
     */
    fun validateUser(user: User): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (user.name.isBlank()) {
            errors.add("Name is required")
        }
        
        if (user.email.isBlank()) {
            errors.add("Email is required")
        } else if (!user.email.contains("@")) {
            errors.add("Invalid email format")
        }
        
        if (user.department.isBlank()) {
            errors.add("Department is required")
        }
        
        if (user.role.isBlank()) {
            errors.add("Role is required")
        }
        
        if (user.salary < 0) {
            errors.add("Salary must be positive")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    /**
     * Generate analytics report
     */
    fun generateAnalyticsReport(): AnalyticsReport {
        val users = _userList.value
        
        return AnalyticsReport(
            totalUsers = users.size,
            activeUsers = users.count { it.isActive },
            inactiveUsers = users.count { !it.isActive },
            averageSalary = users.map { it.salary }.average(),
            departmentDistribution = users.groupBy { it.department }.mapValues { it.value.size },
            roleDistribution = users.groupBy { it.role }.mapValues { it.value.size },
            averagePerformance = users.map { it.performanceRating }.average(),
            topPerformers = users.sortedByDescending { it.performanceRating }.take(5),
            skillsDistribution = users.flatMap { it.skills }.groupBy { it }.mapValues { it.value.size },
            generatedAt = System.currentTimeMillis()
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up resources
        println("TestViewModel cleared")
    }
}

// Data classes and enums
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val department: String,
    val role: String,
    val isActive: Boolean,
    val joinDate: Long,
    val salary: Int,
    val profileImageUrl: String,
    val phoneNumber: String,
    val address: String,
    val skills: List<String>,
    val projects: List<String>,
    val performanceRating: Double
)

data class TestUiState(
    val isInitialized: Boolean = false,
    val lastUpdated: Long = 0L,
    val selectedUserId: Int? = null,
    val connectionStatus: String = "Connected",
    val refreshCount: Int = 0
)

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType,
    val isRead: Boolean
)

enum class NotificationType {
    INFO, SUCCESS, WARNING, ERROR
}

enum class ExportFormat {
    CSV, JSON, PDF, EXCEL
}

enum class ImportSource {
    CSV, JSON, DATABASE, API
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: List<String>) : ValidationResult()
}

data class AnalyticsReport(
    val totalUsers: Int,
    val activeUsers: Int,
    val inactiveUsers: Int,
    val averageSalary: Double,
    val departmentDistribution: Map<String, Int>,
    val roleDistribution: Map<String, Int>,
    val averagePerformance: Double,
    val topPerformers: List<User>,
    val skillsDistribution: Map<String, Int>,
    val generatedAt: Long
)

// Repository simulation class
class TestRepository {
    fun fetchUsers(): List<User> {
        // Simulate repository operations
        return emptyList()
    }
    
    fun saveUser(user: User): Boolean {
        // Simulate save operation
        return true
    }
    
    fun deleteUser(id: Int): Boolean {
        // Simulate delete operation
        return true
    }
}

// Factory for creating TestViewModel
class TestViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TestViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}