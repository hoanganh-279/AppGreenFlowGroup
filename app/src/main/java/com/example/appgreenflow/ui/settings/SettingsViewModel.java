package com.example.appgreenflow.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> notificationsEnabled = new MutableLiveData<>(true);
    private final MutableLiveData<String> selectedLanguage = new MutableLiveData<>("Tiếng Việt");
    private final MutableLiveData<Integer> selectedLanguageIndex = new MutableLiveData<>(0);

    public void setDarkModeEnabled(boolean enabled) {
        darkModeEnabled.setValue(enabled);
        // TODO: Lưu vào SharedPreferences
    }

    public LiveData<Boolean> isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public void setNotificationsEnabled(boolean enabled) {
        notificationsEnabled.setValue(enabled);
        // TODO: Lưu vào SharedPreferences
    }

    public LiveData<Boolean> isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setSelectedLanguage(String language) {
        selectedLanguage.setValue(language);
        // TODO: Lưu vào SharedPreferences và cập nhật locale
    }

    public LiveData<String> getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguageIndex(int index) {
        selectedLanguageIndex.setValue(index);
    }

    public LiveData<Integer> getSelectedLanguageIndex() {
        return selectedLanguageIndex;
    }
}