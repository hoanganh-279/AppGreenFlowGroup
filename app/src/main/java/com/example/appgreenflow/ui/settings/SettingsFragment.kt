package com.example.appgreenflow.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CompoundButton
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
        mViewModel = ViewModelProvider(this).get<SettingsViewModel?>(SettingsViewModel::class.java)
        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        userRole = (requireActivity() as MainActivity).getUserRole() // Lấy role
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        switchDarkMode = rootView.findViewById<SwitchMaterial>(R.id.switchDarkMode)
        switchNotifications = rootView.findViewById<SwitchMaterial>(R.id.switchNotifications)
        switchAutoRoute =
            rootView.findViewById<SwitchMaterial?>(R.id.switchAutoRoute) // ID mới cho auto-route
        spinnerLanguage = rootView.findViewById<Spinner>(R.id.spinnerLanguage)
        layoutAccountInfo = rootView.findViewById<LinearLayout>(R.id.layoutAccountInfo)
        layoutAbout = rootView.findViewById<LinearLayout>(R.id.layoutAbout)

        switchDarkMode!!.setChecked(prefs!!.getBoolean("dark_mode", false))
        switchNotifications!!.setChecked(prefs!!.getBoolean("notifications", true))
        if (switchAutoRoute != null) {
            switchAutoRoute!!.setChecked(prefs!!.getBoolean("auto_route", false)) // Default false
        }
        spinnerLanguage!!.setSelection(prefs!!.getInt("language_index", 0))

        if (switchAutoRoute != null) {
            switchAutoRoute!!.setVisibility(if ("employee" == userRole) View.VISIBLE else View.GONE)
        }

        switchDarkMode!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            prefs!!.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            requireActivity().recreate()
        })

        switchNotifications!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            prefs!!.edit().putBoolean("notifications", isChecked).apply()
            // TODO: Subscribe/unsubscribe FCM
            Toast.makeText(
                getContext(),
                if (isChecked) "Thông báo bật" else "Thông báo tắt",
                Toast.LENGTH_SHORT
            ).show()
        })

        // Role-specific listener cho auto-route
        if (switchAutoRoute != null) {
            switchAutoRoute!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                prefs!!.edit().putBoolean("auto_route", isChecked).apply()
                Toast.makeText(
                    getContext(),
                    if (isChecked) "Chế độ tuyến đường tự động bật" else "Chế độ tuyến đường tự động tắt",
                    Toast.LENGTH_SHORT
                ).show()
            })
        }

        spinnerLanguage!!.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                prefs!!.edit().putInt("language_index", position).apply()
                // TODO: Change locale
                Toast.makeText(
                    getContext(),
                    "Ngôn ngữ: " + parent.getItemAtPosition(position),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        layoutAccountInfo!!.setOnClickListener(View.OnClickListener { v: View? ->
            Toast.makeText(
                getContext(),
                "Xem tài khoản",
                Toast.LENGTH_SHORT
            ).show()
        }) // TODO: Navigate profile
        layoutAbout!!.setOnClickListener(View.OnClickListener { v: View? ->
            Toast.makeText(
                getContext(),
                "Về GreenFlow v1.0",
                Toast.LENGTH_SHORT
            ).show()
        }) // TODO: Dialog about

        return rootView
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}