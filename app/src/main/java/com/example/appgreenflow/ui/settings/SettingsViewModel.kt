package com.example.appgreenflow.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    private val darkModeEnabled = MutableLiveData<Boolean?>(false)
    private val notificationsEnabled = MutableLiveData<Boolean?>(true)
    private val autoRouteEnabled = MutableLiveData<Boolean?>(false) // Thêm cho employee
    private val selectedLanguage = MutableLiveData<String?>("Tiếng Việt")
    private val selectedLanguageIndex = MutableLiveData<Int?>(0)

    fun setDarkModeEnabled(enabled: Boolean) {
        darkModeEnabled.setValue(enabled)
        // TODO: Lưu vào SharedPreferences
    }

    fun isDarkModeEnabled(): LiveData<Boolean?> {
        return darkModeEnabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled.setValue(enabled)
        // TODO: Lưu vào SharedPreferences
    }

    fun isNotificationsEnabled(): LiveData<Boolean?> {
        return notificationsEnabled
    }

    fun setAutoRouteEnabled(enabled: Boolean) {
        autoRouteEnabled.setValue(enabled)
        // TODO: Lưu vào SharedPreferences và trigger logic route
    }

    fun isAutoRouteEnabled(): LiveData<Boolean?> {
        return autoRouteEnabled
    }

    fun setSelectedLanguage(language: String?) {
        selectedLanguage.setValue(language)
        // TODO: Lưu vào SharedPreferences và cập nhật locale
    }

    fun getSelectedLanguage(): LiveData<String?> {
        return selectedLanguage
    }

    fun setSelectedLanguageIndex(index: Int) {
        selectedLanguageIndex.setValue(index)
    }

    fun getSelectedLanguageIndex(): LiveData<Int?> {
        return selectedLanguageIndex
    }
}