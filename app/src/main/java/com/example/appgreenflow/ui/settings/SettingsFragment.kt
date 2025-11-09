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
    private var switchTrashFullAlert: SwitchMaterial? = null
    private var switchAutoRoute: SwitchMaterial? = null
    private var spinnerLanguage: Spinner? = null
    private var layoutAccountInfo: LinearLayout? = null
    private var layoutChangePassword: LinearLayout? = null
    private var layoutAbout: LinearLayout? = null
    private var layoutHelp: LinearLayout? = null
    private var cardAdvanced: View? = null
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
        switchTrashFullAlert = rootView.findViewById(R.id.switchTrashFullAlert)
        switchAutoRoute = rootView.findViewById(R.id.switchAutoRoute)
        spinnerLanguage = rootView.findViewById(R.id.spinnerLanguage)
        layoutAccountInfo = rootView.findViewById(R.id.layoutAccountInfo)
        layoutChangePassword = rootView.findViewById(R.id.layoutChangePassword)
        layoutAbout = rootView.findViewById(R.id.layoutAbout)
        layoutHelp = rootView.findViewById(R.id.layoutHelp)
        cardAdvanced = rootView.findViewById(R.id.cardAdvanced)

        // Load preferences
        switchDarkMode?.isChecked = prefs?.getBoolean("dark_mode", false) ?: false
        switchNotifications?.isChecked = prefs?.getBoolean("notifications", true) ?: true
        switchTrashFullAlert?.isChecked = prefs?.getBoolean("trash_full_alert", true) ?: true
        switchAutoRoute?.isChecked = prefs?.getBoolean("auto_route", false) ?: false
        spinnerLanguage?.setSelection(prefs?.getInt("language_index", 0) ?: 0)

        // Show advanced features for employee
        cardAdvanced?.visibility = if (userRole == "employee") View.VISIBLE else View.GONE

        switchDarkMode?.setOnCheckedChangeListener { _, isChecked ->
            prefs?.edit()?.putBoolean("dark_mode", isChecked)?.apply()
            AppCompatDelegate.setDefaultNightMode(if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            requireActivity().recreate()
        }

        switchNotifications?.setOnCheckedChangeListener { _, isChecked ->
            prefs?.edit()?.putBoolean("notifications", isChecked)?.apply()
            Toast.makeText(
                context,
                if (isChecked) "Đã bật thông báo" else "Đã tắt thông báo",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        switchTrashFullAlert?.setOnCheckedChangeListener { _, isChecked ->
            prefs?.edit()?.putBoolean("trash_full_alert", isChecked)?.apply()
            Toast.makeText(
                context,
                if (isChecked) "Đã bật cảnh báo thùng rác đầy" else "Đã tắt cảnh báo thùng rác đầy",
                Toast.LENGTH_SHORT
            ).show()
        }

        switchAutoRoute?.setOnCheckedChangeListener { _, isChecked ->
            prefs?.edit()?.putBoolean("auto_route", isChecked)?.apply()
            Toast.makeText(
                context,
                if (isChecked) "Đã bật tối ưu lộ trình tự động" else "Đã tắt tối ưu lộ trình tự động",
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
            showAccountInfo()
        }
        
        layoutChangePassword?.setOnClickListener {
            showChangePasswordDialog()
        }
        
        layoutAbout?.setOnClickListener {
            showAboutDialog()
        }
        
        layoutHelp?.setOnClickListener {
            Toast.makeText(context, "Mở chat hỗ trợ...", Toast.LENGTH_SHORT).show()
            // Mở ChatActivity
            val intent = android.content.Intent(requireContext(), com.example.appgreenflow.ChatActivity::class.java)
            startActivity(intent)
        }
        
        // Thêm chat button
        activity?.let { act ->
            com.example.appgreenflow.ChatHelper.addChatButton(act)
        }

        return rootView
    }
    
    private fun showAccountInfo() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val message = """
            Tên: ${user?.displayName ?: "Chưa cập nhật"}
            Email: ${user?.email ?: "Không có"}
            Vai trò: ${userRole ?: "customer"}
            Trạng thái: ${if (user?.isEmailVerified == true) "Đã xác thực" else "Chưa xác thực"}
        """.trimIndent()
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Thông tin tài khoản")
            .setMessage(message)
            .setPositiveButton("Đóng", null)
            .show()
    }
    
    private fun showChangePasswordDialog() {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user?.email == null) {
            Toast.makeText(context, "Không thể đổi mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Đổi mật khẩu")
            .setMessage("Chúng tôi sẽ gửi email hướng dẫn đổi mật khẩu đến ${user.email}")
            .setPositiveButton("Gửi email") { _, _ ->
                com.google.firebase.auth.FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(user.email!!)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Đã gửi email đổi mật khẩu!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun showAboutDialog() {
        val message = """
            🌿 GreenFlow
            Phiên bản: 1.0.0
            
            Ứng dụng quản lý thu gom rác thải thông minh, giúp bảo vệ môi trường và tối ưu hóa quy trình thu gom.
            
            © 2024 GreenFlow Team
            
            Liên hệ:
            📧 support@greenflow.vn
            📞 1900-xxxx
        """.trimIndent()
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Về GreenFlow")
            .setMessage(message)
            .setPositiveButton("Đóng", null)
            .show()
    }

    companion object {
        @Suppress("UNUSED")  // Suppress if not used
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}