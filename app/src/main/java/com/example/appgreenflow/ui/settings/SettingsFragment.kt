package com.example.appgreenflow.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appgreenflow.MainActivity
import com.example.appgreenflow.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {
    private var mViewModel: SettingsViewModel? = null
    private var switchDarkMode: SwitchMaterial? = null
    private var switchNotifications: SwitchMaterial? = null
    private var switchAutoRoute: SwitchMaterial? = null // Thêm switchAutoRoute cho employee
    private var spinnerLanguage: Spinner? = null
    private var layoutAccountInfo: LinearLayout? = null
    private var layoutAbout: LinearLayout? = null
    private var prefs: SharedPreferences? = null
    private var userRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        userRole = (requireActivity() as MainActivity).userRole.orEmpty() // Changed to property with orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        switchDarkMode = rootView.findViewById(R.id.switchDarkMode)
        switchNotifications = rootView.findViewById(R.id.switchNotifications)
        switchAutoRoute = rootView.findViewById(R.id.switchAutoRoute) // ID mới cho auto-route
        spinnerLanguage = rootView.findViewById(R.id.spinnerLanguage)
        layoutAccountInfo = rootView.findViewById(R.id.layoutAccountInfo)
        layoutAbout = rootView.findViewById(R.id.layoutAbout)

        switchDarkMode?.isChecked = prefs?.getBoolean("dark_mode", false) ?: false
        switchNotifications?.isChecked = prefs?.getBoolean("notifications", true) ?: true
        switchAutoRoute?.isChecked = prefs?.getBoolean("auto_route", false) ?: false // Default false
        spinnerLanguage?.setSelection(prefs?.getInt("language_index", 0) ?: 0)

        switchAutoRoute?.visibility = if (userRole == "employee") View.VISIBLE else View.GONE

        switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
            prefs?.edit()?.putBoolean("dark_mode", isChecked)?.apply()
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            requireActivity().recreate()
        }

        switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            prefs?.edit()?.putBoolean("notifications", isChecked)?.apply()
            // TODO: Subscribe/unsubscribe FCM
            Toast.makeText(
                context,
                if (isChecked) "Thông báo bật" else "Thông báo tắt",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Role-specific listener cho auto-route
        switchAutoRoute?.setOnCheckedChangeListener { _, isChecked ->
            prefs?.edit()?.putBoolean("auto_route", isChecked)?.apply()
            Toast.makeText(
                context,
                if (isChecked) "Chế độ tuyến đường tự động bật" else "Chế độ tuyến đường tự động tắt",
                Toast.LENGTH_SHORT
            ).show()
        }

        spinnerLanguage?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                prefs?.edit()?.putInt("language_index", position)?.apply()
                // TODO: Change locale
                Toast.makeText(
                    context,
                    "Ngôn ngữ: ${parent.getItemAtPosition(position)}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        layoutAccountInfo?.setOnClickListener {
            Toast.makeText(
                context,
                "Xem tài khoản",
                Toast.LENGTH_SHORT
            ).show()
        } // TODO: Navigate profile
        layoutAbout?.setOnClickListener {
            Toast.makeText(
                context,
                "Về GreenFlow v1.0",
                Toast.LENGTH_SHORT
            ).show()
        } // TODO: Dialog about

        return rootView
    }

    companion object {
        @Suppress("UNUSED")  // Suppress if not used
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}