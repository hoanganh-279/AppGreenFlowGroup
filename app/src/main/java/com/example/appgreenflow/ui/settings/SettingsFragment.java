package com.example.appgreenflow.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.appgreenflow.MainActivity;
import com.example.appgreenflow.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;
    private SwitchMaterial switchDarkMode, switchNotifications, switchAutoRoute;  // Thêm switchAutoRoute cho employee
    private Spinner spinnerLanguage;
    private LinearLayout layoutAccountInfo, layoutAbout;
    private SharedPreferences prefs;
    private String userRole;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        userRole = ((MainActivity) requireActivity()).getUserRole();  // Lấy role
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        switchDarkMode = rootView.findViewById(R.id.switchDarkMode);
        switchNotifications = rootView.findViewById(R.id.switchNotifications);
        switchAutoRoute = rootView.findViewById(R.id.switchAutoRoute);  // ID mới cho auto-route
        spinnerLanguage = rootView.findViewById(R.id.spinnerLanguage);
        layoutAccountInfo = rootView.findViewById(R.id.layoutAccountInfo);
        layoutAbout = rootView.findViewById(R.id.layoutAbout);

        // Load saved values
        switchDarkMode.setChecked(prefs.getBoolean("dark_mode", false));
        switchNotifications.setChecked(prefs.getBoolean("notifications", true));
        if (switchAutoRoute != null) {
            switchAutoRoute.setChecked(prefs.getBoolean("auto_route", false));  // Default false
        }
        spinnerLanguage.setSelection(prefs.getInt("language_index", 0));

        // Role-specific: Ẩn/hiện auto-route cho employee
        if (switchAutoRoute != null) {
            switchAutoRoute.setVisibility("employee".equals(userRole) ? View.VISIBLE : View.GONE);
        }

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            requireActivity().recreate();
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications", isChecked).apply();
            // TODO: Subscribe/unsubscribe FCM
            Toast.makeText(getContext(), isChecked ? "Thông báo bật" : "Thông báo tắt", Toast.LENGTH_SHORT).show();
        });

        // Role-specific listener cho auto-route
        if (switchAutoRoute != null) {
            switchAutoRoute.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("auto_route", isChecked).apply();
                Toast.makeText(getContext(), isChecked ? "Chế độ tuyến đường tự động bật" : "Chế độ tuyến đường tự động tắt", Toast.LENGTH_SHORT).show();
                // TODO: Trigger auto-route logic ở RouteFragment nếu cần
            });
        }

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt("language_index", position).apply();
                // TODO: Change locale
                Toast.makeText(getContext(), "Ngôn ngữ: " + parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        layoutAccountInfo.setOnClickListener(v -> Toast.makeText(getContext(), "Xem tài khoản", Toast.LENGTH_SHORT).show());  // TODO: Navigate profile
        layoutAbout.setOnClickListener(v -> Toast.makeText(getContext(), "Về GreenFlow v1.0", Toast.LENGTH_SHORT).show());  // TODO: Dialog about

        return rootView;
    }
}