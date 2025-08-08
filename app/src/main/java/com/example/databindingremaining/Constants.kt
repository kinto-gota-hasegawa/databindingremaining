package com.example.databindingremaining

object Constants {
    
    const val API_BASE_URL = "https://api.example.com/v1"
    const val API_TIMEOUT = 30000
    const val API_KEY = "your_api_key_here"
    
    const val DATABASE_NAME = "app_database"
    const val DATABASE_VERSION = 1
    
    const val PREF_DARK_MODE = "pref_dark_mode"
    const val PREF_NOTIFICATIONS = "pref_notifications"
    const val PREF_AUTO_SYNC = "pref_auto_sync"
    const val PREF_LANGUAGE = "pref_language"
    const val PREF_SYNC_INTERVAL = "pref_sync_interval"
    const val PREF_LAST_SETTINGS_UPDATE = "pref_last_settings_update"
    const val PREF_USER_ID = "pref_user_id"
    const val PREF_USER_TOKEN = "pref_user_token"
    const val PREF_FIRST_LAUNCH = "pref_first_launch"
    const val PREF_CACHE_EXPIRY = "pref_cache_expiry"
    
    const val INTENT_EXTRA_USER = "extra_user"
    const val INTENT_EXTRA_USER_ID = "extra_user_id"
    const val INTENT_EXTRA_EDIT_MODE = "extra_edit_mode"
    const val INTENT_EXTRA_MESSAGE = "extra_message"
    
    const val REQUEST_CODE_CAMERA = 1001
    const val REQUEST_CODE_GALLERY = 1002
    const val REQUEST_CODE_PERMISSIONS = 1003
    const val REQUEST_CODE_LOCATION = 1004
    
    const val MAX_FILE_SIZE = 10 * 1024 * 1024
    const val MAX_IMAGE_WIDTH = 1920
    const val MAX_IMAGE_HEIGHT = 1080
    const val IMAGE_COMPRESSION_QUALITY = 80
    
    const val CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000L
    const val SESSION_TIMEOUT = 30 * 60 * 1000L
    const val REFRESH_INTERVAL = 5 * 60 * 1000L
    
    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_LOGIN_ATTEMPTS = 3
    const val LOCKOUT_DURATION = 15 * 60 * 1000L
    
    const val PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 40
    const val PREFETCH_DISTANCE = 10
    
    const val TAG_DEBUG = "DEBUG"
    const val TAG_ERROR = "ERROR"
    const val TAG_INFO = "INFO"
    const val TAG_WARNING = "WARNING"
    
    const val DATE_FORMAT_DEFAULT = "yyyy-MM-dd"
    const val DATE_FORMAT_DISPLAY = "dd MMM yyyy"
    const val TIME_FORMAT_DEFAULT = "HH:mm:ss"
    const val TIME_FORMAT_DISPLAY = "hh:mm a"
    const val DATETIME_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss"
    
    const val NOTIFICATION_CHANNEL_ID = "app_notification_channel"
    const val NOTIFICATION_CHANNEL_NAME = "App Notifications"
    const val NOTIFICATION_ID_SYNC = 1001
    const val NOTIFICATION_ID_MESSAGE = 1002
    const val NOTIFICATION_ID_UPDATE = 1003
    
    const val PERMISSION_CAMERA = android.Manifest.permission.CAMERA
    const val PERMISSION_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE
    const val PERMISSION_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_NOTIFICATIONS = android.Manifest.permission.POST_NOTIFICATIONS
    
    const val REGEX_EMAIL = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    const val REGEX_PHONE = "^[+]?[0-9]{10,15}$"
    const val REGEX_URL = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
    
    const val ERROR_NETWORK = "Network error. Please check your connection."
    const val ERROR_SERVER = "Server error. Please try again later."
    const val ERROR_UNAUTHORIZED = "Unauthorized access. Please login again."
    const val ERROR_NOT_FOUND = "Resource not found."
    const val ERROR_VALIDATION = "Validation error. Please check your input."
    const val ERROR_UNKNOWN = "An unknown error occurred."
    
    const val SUCCESS_SAVED = "Data saved successfully."
    const val SUCCESS_DELETED = "Data deleted successfully."
    const val SUCCESS_UPDATED = "Data updated successfully."
    const val SUCCESS_SYNCED = "Data synced successfully."
    
    val SUPPORTED_IMAGE_FORMATS = listOf("jpg", "jpeg", "png", "gif", "webp")
    val SUPPORTED_VIDEO_FORMATS = listOf("mp4", "avi", "mov", "mkv")
    val SUPPORTED_AUDIO_FORMATS = listOf("mp3", "wav", "aac", "ogg")
    
    enum class UserRole {
        ADMIN,
        MODERATOR,
        USER,
        GUEST
    }
    
    enum class SyncStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
    
    enum class NetworkType {
        WIFI,
        MOBILE,
        ETHERNET,
        NONE
    }
}