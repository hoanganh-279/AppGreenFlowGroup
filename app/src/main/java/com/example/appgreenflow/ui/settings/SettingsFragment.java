package com.example.appgreenflow.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appgreenflow.R;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;
    private Switch switchDarkMode, switchNotifications;
    private Spinner spinnerLanguage;
    private LinearLayout layoutAccountInfo, layoutAbout;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        switchDarkMode = rootView.findViewById(R.id.switchDarkMode);
        switchNotifications = rootView.findViewById(R.id.switchNotifications);
        spinnerLanguage = rootView.findViewById(R.id.spinnerLanguage);
        layoutAccountInfo = rootView.findViewById(R.id.layoutAccountInfo);
        layoutAbout = rootView.findViewById(R.id.layoutAbout);


        if (switchDarkMode != null) {
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mViewModel.setDarkModeEnabled(isChecked);
                Toast.makeText(getContext(), isChecked ? "Chế độ tối đã bật" : "Chế độ tối đã tắt", Toast.LENGTH_SHORT).show();
                // TODO: Áp dụng theme dark/light cho app
            });
        }

        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mViewModel.setNotificationsEnabled(isChecked);
                Toast.makeText(getContext(), isChecked ? "Nhận thông báo đã bật" : "Nhận thông báo đã tắt", Toast.LENGTH_SHORT).show();
                // TODO: Cập nhật SharedPreferences hoặc Firebase
            });
        }

        if (spinnerLanguage != null) {
            spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedLanguage = parent.getItemAtPosition(position).toString();
                    mViewModel.setSelectedLanguage(selectedLanguage);
                    Toast.makeText(getContext(), "Ngôn ngữ: " + selectedLanguage, Toast.LENGTH_SHORT).show();
                    // TODO: Thay đổi locale của app
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Không làm gì
                }
            });
        }

        if (layoutAccountInfo != null) {
            layoutAccountInfo.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Xem thông tin tài khoản", Toast.LENGTH_SHORT).show();
                // TODO: Navigate đến Profile
            });
        }

        if (layoutAbout != null) {
            layoutAbout.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Thông tin về ứng dụng", Toast.LENGTH_SHORT).show();
                // TODO: Mở dialog About
            });
        }

        if (switchDarkMode != null) {
            Boolean darkEnabled = mViewModel.isDarkModeEnabled().getValue();
            switchDarkMode.setChecked(darkEnabled != null ? darkEnabled : false);
        }
        if (switchNotifications != null) {
            Boolean notifEnabled = mViewModel.isNotificationsEnabled().getValue();
            switchNotifications.setChecked(notifEnabled != null ? notifEnabled : true);
        }
        if (spinnerLanguage != null) {
            Integer index = mViewModel.getSelectedLanguageIndex().getValue();
            if (index != null) {
                spinnerLanguage.setSelection(index);
            }
        }

        return rootView;
    }
}