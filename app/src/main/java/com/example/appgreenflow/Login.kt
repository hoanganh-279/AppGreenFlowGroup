package com.example.appgreenflow

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {
    private var editEmailLogin: TextInputEditText? = null
    private var editPasswordLogin: TextInputEditText? = null
    private var emailInputLayout: TextInputLayout? = null
    private var passwordInputLayout: TextInputLayout? = null
    private var loginBtn: Button? = null
    private var spinnerRole: Spinner? = null
    private var progressBar: ProgressBar? = null
    private var registerNow: TextView? = null
    private var forgotPassword: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private var selectedRole = "customer"

    public override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            // Không kiểm tra email verified - cho phép đăng nhập ngay
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        initViews()
        setupSpinner()
        setupClickListeners()
        
        // Thêm chat button
        ChatHelper.addChatButton(this)
    }

    private fun initViews() {
        editEmailLogin = findViewById(R.id.editEmailLogin)
        editPasswordLogin = findViewById(R.id.editPasswordLogin)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        loginBtn = findViewById(R.id.loginBtn)
        spinnerRole = findViewById(R.id.spinnerRole)
        progressBar = findViewById(R.id.progressBar)
        registerNow = findViewById(R.id.registerNow)
        forgotPassword = findViewById(R.id.forgotPassword)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.user_roles,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole?.adapter = adapter
        spinnerRole?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> selectedRole = "customer"
                    1 -> selectedRole = "employee"
                    2 -> {
                        Toast.makeText(this@Login, "Vai trò không hợp lệ!", Toast.LENGTH_SHORT).show()
                        selectedRole = "customer"
                        spinnerRole?.setSelection(0)
                        return
                    }
                }
                registerNow?.isEnabled = selectedRole == "customer"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupClickListeners() {
        loginBtn?.setOnClickListener {
            clearErrors()
            if (validateInputs()) {
                handleLogin()
            }
        }
        
        registerNow?.setOnClickListener {
            if (selectedRole == "employee") {
                Toast.makeText(this, "Tài khoản nhân viên do admin tạo!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, Register::class.java)
            intent.putExtra("role", selectedRole)
            startActivity(intent)
        }
        
        forgotPassword?.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun clearErrors() {
        emailInputLayout?.error = null
        passwordInputLayout?.error = null
    }

    private fun validateInputs(): Boolean {
        val email = editEmailLogin?.text.toString().trim()
        val password = editPasswordLogin?.text.toString()
        var isValid = true

        if (TextUtils.isEmpty(email)) {
            emailInputLayout?.error = "Vui lòng nhập email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout?.error = "Email không hợp lệ"
            isValid = false
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout?.error = "Vui lòng nhập mật khẩu"
            isValid = false
        }

        return isValid
    }

    private fun handleLogin() {
        val email = editEmailLogin?.text.toString().trim()
        val password = editPasswordLogin?.text.toString()
        
        progressBar?.visibility = View.VISIBLE
        loginBtn?.isEnabled = false

        mAuth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth?.currentUser
                    
                    // Không kiểm tra email verification nữa - cho phép đăng nhập ngay
                    // Validate role
                    validateUserRole(user?.uid ?: "")
                } else {
                    progressBar?.visibility = View.GONE
                    loginBtn?.isEnabled = true
                    
                    val errorMessage = when {
                        task.exception?.message?.contains("password") == true -> 
                            "Email hoặc mật khẩu không đúng"
                        task.exception?.message?.contains("user") == true -> 
                            "Tài khoản không tồn tại"
                        task.exception?.message?.contains("network") == true -> 
                            "Lỗi kết nối mạng"
                        else -> "Đăng nhập thất bại: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validateUserRole(uid: String) {
        db?.collection("users")?.document(uid)?.get()
            ?.addOnSuccessListener { doc ->
                progressBar?.visibility = View.GONE
                loginBtn?.isEnabled = true
                
                if (doc.exists()) {
                    val storedRole = doc.getString("role")
                    if (selectedRole != storedRole) {
                        Toast.makeText(
                            this,
                            "Vai trò không khớp! Vui lòng chọn đúng vai trò.",
                            Toast.LENGTH_LONG
                        ).show()
                        mAuth?.signOut()
                        return@addOnSuccessListener
                    }
                    
                    // Update email verified status in Firestore
                    doc.reference.update("emailVerified", true)
                    
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Dữ liệu người dùng không tồn tại!", Toast.LENGTH_SHORT).show()
                    mAuth?.signOut()
                }
            }
            ?.addOnFailureListener { e ->
                progressBar?.visibility = View.GONE
                loginBtn?.isEnabled = true
                Toast.makeText(this, "Lỗi kiểm tra dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEmailNotVerifiedDialog(email: String) {
        AlertDialog.Builder(this)
            .setTitle("Email chưa xác thực")
            .setMessage("Vui lòng xác thực email của bạn trước khi đăng nhập.\n\n" +
                    "Kiểm tra hộp thư $email và nhấn vào link xác thực.")
            .setPositiveButton("Gửi lại email") { dialog, _ ->
                dialog.dismiss()
                resendVerificationEmail(email)
            }
            .setNegativeButton("Đóng") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun resendVerificationEmail(email: String) {
        progressBar?.visibility = View.VISIBLE
        
        // Sign in temporarily to send verification
        val password = editPasswordLogin?.text.toString()
        mAuth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mAuth?.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { verifyTask ->
                            progressBar?.visibility = View.GONE
                            mAuth?.signOut()
                            
                            if (verifyTask.isSuccessful) {
                                Toast.makeText(this, "Đã gửi lại email xác thực!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Lỗi gửi email!", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    progressBar?.visibility = View.GONE
                    Toast.makeText(this, "Lỗi xác thực!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.emailInput)
        val emailLayout = dialogView.findViewById<TextInputLayout>(R.id.emailInputLayout)
        
        AlertDialog.Builder(this)
            .setTitle("Quên mật khẩu")
            .setMessage("Nhập email của bạn để nhận link đặt lại mật khẩu")
            .setView(dialogView)
            .setPositiveButton("Gửi") { dialog, _ ->
                val email = emailInput.text.toString().trim()
                
                if (TextUtils.isEmpty(email)) {
                    emailLayout.error = "Vui lòng nhập email"
                    return@setPositiveButton
                }
                
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailLayout.error = "Email không hợp lệ"
                    return@setPositiveButton
                }
                
                dialog.dismiss()
                sendPasswordResetEmail(email)
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sendPasswordResetEmail(email: String) {
        progressBar?.visibility = View.VISIBLE
        
        mAuth?.sendPasswordResetEmail(email)
            ?.addOnCompleteListener { task ->
                progressBar?.visibility = View.GONE
                
                if (task.isSuccessful) {
                    AlertDialog.Builder(this)
                        .setTitle("Email đã gửi")
                        .setMessage("Chúng tôi đã gửi link đặt lại mật khẩu đến $email.\n\n" +
                                "Vui lòng kiểm tra hộp thư và làm theo hướng dẫn.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("user") == true -> 
                            "Email không tồn tại trong hệ thống"
                        else -> "Lỗi gửi email: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}
