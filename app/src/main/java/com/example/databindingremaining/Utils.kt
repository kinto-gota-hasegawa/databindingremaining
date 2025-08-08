package com.example.databindingremaining

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.format.DateFormat
import android.util.Patterns
import android.widget.Toast
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object Utils {
    
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo?.isConnected == true
        }
    }
    
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
    
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPassword(password: String): Boolean {
        val minLength = 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        
        return password.length >= minLength && 
               hasUpperCase && 
               hasLowerCase && 
               hasDigit && 
               hasSpecialChar
    }
    
    fun formatDate(timestamp: Long, pattern: String = "dd/MM/yyyy"): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
    
    fun formatDateTime(timestamp: Long): String {
        return formatDate(timestamp, "dd/MM/yyyy HH:mm:ss")
    }
    
    fun getRelativeTimeString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "just now"
            diff < 3_600_000 -> "${diff / 60_000} minutes ago"
            diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
            diff < 604_800_000 -> "${diff / 86_400_000} days ago"
            diff < 2_592_000_000 -> "${diff / 604_800_000} weeks ago"
            diff < 31_536_000_000 -> "${diff / 2_592_000_000} months ago"
            else -> "${diff / 31_536_000_000} years ago"
        }
    }
    
    fun md5Hash(text: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(text.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    fun sha256Hash(text: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(text.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    fun dpToPx(context: Context, dp: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).roundToInt()
    }
    
    fun pxToDp(context: Context, px: Float): Int {
        val density = context.resources.displayMetrics.density
        return (px / density).roundToInt()
    }
    
    fun isTablet(context: Context): Boolean {
        val displayMetrics = context.resources.displayMetrics
        val widthDp = displayMetrics.widthPixels / displayMetrics.density
        val heightDp = displayMetrics.heightPixels / displayMetrics.density
        val screenSize = Math.sqrt((widthDp * widthDp + heightDp * heightDp).toDouble())
        return screenSize >= 7.0
    }
    
    fun capitalizeFirstLetter(text: String): String {
        return if (text.isEmpty()) {
            text
        } else {
            text.substring(0, 1).uppercase() + text.substring(1).lowercase()
        }
    }
    
    fun capitalizeWords(text: String): String {
        return text.split(" ").joinToString(" ") { capitalizeFirstLetter(it) }
    }
    
    fun generateRandomString(length: Int = 10): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
    
    fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false
        
        for (processInfo in runningAppProcesses) {
            if (processInfo.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                processInfo.processName == context.packageName) {
                return true
            }
        }
        return false
    }
    
    fun parseStringToInt(value: String, defaultValue: Int = 0): Int {
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }
    
    fun parseStringToDouble(value: String, defaultValue: Double = 0.0): Double {
        return try {
            value.toDouble()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }
}