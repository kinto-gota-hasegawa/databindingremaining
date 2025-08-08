package com.example.databindingremaining

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

class SettingsFragment : Fragment() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var darkModeSwitch: Switch
    private lateinit var notificationSwitch: Switch
    private lateinit var autoSyncSwitch: Switch
    private lateinit var languageSpinner: Spinner
    private lateinit var syncIntervalSpinner: Spinner
    private lateinit var cacheButton: Button
    private lateinit var resetButton: Button
    private lateinit var versionTextView: TextView
    private lateinit var storageTextView: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        
        initializeViews(view)
        loadSettings()
        setupListeners()
        updateStorageInfo()
    }
    
    private fun initializeViews(view: View) {
        darkModeSwitch = view.findViewById(R.id.switchDarkMode)
        notificationSwitch = view.findViewById(R.id.switchNotifications)
        autoSyncSwitch = view.findViewById(R.id.switchAutoSync)
        languageSpinner = view.findViewById(R.id.spinnerLanguage)
        syncIntervalSpinner = view.findViewById(R.id.spinnerSyncInterval)
        cacheButton = view.findViewById(R.id.buttonClearCache)
        resetButton = view.findViewById(R.id.buttonResetSettings)
        versionTextView = view.findViewById(R.id.textViewVersion)
        storageTextView = view.findViewById(R.id.textViewStorage)
        
        setupSpinners()
        displayAppVersion()
    }
    
    private fun setupSpinners() {
        val languages = arrayOf("English", "Japanese", "Spanish", "French", "German")
        val languageAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages
        )
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter
        
        val syncIntervals = arrayOf("5 minutes", "15 minutes", "30 minutes", "1 hour", "3 hours")
        val syncAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            syncIntervals
        )
        syncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        syncIntervalSpinner.adapter = syncAdapter
    }
    
    private fun loadSettings() {
        darkModeSwitch.isChecked = sharedPreferences.getBoolean(Constants.PREF_DARK_MODE, false)
        notificationSwitch.isChecked = sharedPreferences.getBoolean(Constants.PREF_NOTIFICATIONS, true)
        autoSyncSwitch.isChecked = sharedPreferences.getBoolean(Constants.PREF_AUTO_SYNC, true)
        
        val selectedLanguage = sharedPreferences.getInt(Constants.PREF_LANGUAGE, 0)
        languageSpinner.setSelection(selectedLanguage)
        
        val selectedInterval = sharedPreferences.getInt(Constants.PREF_SYNC_INTERVAL, 1)
        syncIntervalSpinner.setSelection(selectedInterval)
    }
    
    private fun setupListeners() {
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference(Constants.PREF_DARK_MODE, isChecked)
            applyDarkMode(isChecked)
        }
        
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference(Constants.PREF_NOTIFICATIONS, isChecked)
            updateNotificationSettings(isChecked)
        }
        
        autoSyncSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanPreference(Constants.PREF_AUTO_SYNC, isChecked)
            syncIntervalSpinner.isEnabled = isChecked
        }
        
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                saveIntPreference(Constants.PREF_LANGUAGE, position)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        syncIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                saveIntPreference(Constants.PREF_SYNC_INTERVAL, position)
                scheduleSyncInterval(position)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        cacheButton.setOnClickListener {
            clearCache()
        }
        
        resetButton.setOnClickListener {
            showResetConfirmationDialog()
        }
    }
    
    private fun saveBooleanPreference(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    private fun saveIntPreference(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    private fun applyDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            activity?.setTheme(R.style.Theme_AppCompat_DayNight_DarkActionBar)
        } else {
            activity?.setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar)
        }
        Utils.showToast(requireContext(), "Theme changed. Restart app to apply.")
    }
    
    private fun updateNotificationSettings(enabled: Boolean) {
        val message = if (enabled) {
            "Notifications enabled"
        } else {
            "Notifications disabled"
        }
        Utils.showToast(requireContext(), message)
    }
    
    private fun scheduleSyncInterval(intervalIndex: Int) {
        val intervals = arrayOf(5, 15, 30, 60, 180)
        val minutes = intervals[intervalIndex]
        Utils.showToast(requireContext(), "Sync interval set to $minutes minutes")
    }
    
    private fun clearCache() {
        try {
            val cacheDir = requireContext().cacheDir
            deleteRecursive(cacheDir)
            updateStorageInfo()
            Utils.showToast(requireContext(), "Cache cleared successfully")
        } catch (e: Exception) {
            Utils.showToast(requireContext(), "Failed to clear cache")
        }
    }
    
    private fun deleteRecursive(fileOrDirectory: java.io.File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach { deleteRecursive(it) }
        }
        fileOrDirectory.delete()
    }
    
    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Settings")
            .setMessage("Are you sure you want to reset all settings to default?")
            .setPositiveButton("Yes") { _, _ ->
                resetAllSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun resetAllSettings() {
        sharedPreferences.edit().clear().apply()
        loadSettings()
        Utils.showToast(requireContext(), "Settings reset to default")
    }
    
    private fun displayAppVersion() {
        try {
            val packageInfo = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = packageInfo.versionCode
            versionTextView.text = "Version $versionName ($versionCode)"
        } catch (e: Exception) {
            versionTextView.text = "Version unknown"
        }
    }
    
    private fun updateStorageInfo() {
        val cacheSize = getCacheSize()
        val dataSize = getDataSize()
        val totalSize = cacheSize + dataSize
        
        storageTextView.text = String.format(
            "Cache: %.2f MB | Data: %.2f MB | Total: %.2f MB",
            cacheSize / (1024.0 * 1024.0),
            dataSize / (1024.0 * 1024.0),
            totalSize / (1024.0 * 1024.0)
        )
    }
    
    private fun getCacheSize(): Long {
        return getDirectorySize(requireContext().cacheDir)
    }
    
    private fun getDataSize(): Long {
        return getDirectorySize(requireContext().filesDir)
    }
    
    private fun getDirectorySize(directory: java.io.File): Long {
        var size = 0L
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }
    
    override fun onPause() {
        super.onPause()
        sharedPreferences.edit().putLong(Constants.PREF_LAST_SETTINGS_UPDATE, System.currentTimeMillis()).apply()
    }
}