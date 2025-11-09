package com.example.appgreenflow

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {
    private var editNameRegister: TextInputEditText? = null
    private var editEmailRegister: TextInputEditText? = null
    private var editPasswordRegister: TextInputEditText? = null
    private var editPasswordAgainRegister: TextInputEditText? = null
    private var nameInputLayout: TextInputLayout? = null
    private var emailInputLayout: TextInputLayout? = null
    private var passwordInputLayout: TextInputLayout? = null
    private var passwordAgainInputLayout: TextInputLayout? = null
    private var registerBtn: Button? = null
    private var progressBar: ProgressBar? = null
    private var loginNow: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private var role: String? = "customer"

    public override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            // Không kiểm tra email verified - cho phép vào app ngay
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        role = intent.getStringExtra("role") ?: "customer"
        
        initViews()
        setupClickListeners()
        
        // Thêm chat button
        ChatHelper.addChatButton(this)
    }

    private fun initViews() {
        editNameRegister = findViewById(R.id.editNameRegister)
        editEmailRegister = findViewById(R.id.editEmailRegister)
        editPasswordRegister = findViewById(R.id.editPasswordRegister)
        editPasswordAgainRegister = findViewById(R.id.editPasswordAgainRegister)
        
        // TextInputLayout để hiển thị error
        nameInputLayout = findViewById(R.id.nameInputLayout)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        passwordAgainInputLayout = findViewById(R.id.passwordAgainInputLayout)
        
        registerBtn = findViewById(R.id.registerBtn)
        progressBar = findViewById(R.id.progressBar)
        loginNow = findViewById(R.id.loginNow)
    }

    private fun setupClickListeners() {
        registerBtn?.setOnClickListener { 
            clearErrors()
            if (validateInputs()) {
                handleRegister()
            }
        }
        
        loginNow?.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun clearErrors() {
        nameInputLayout?.error = null
        emailInputLayout?.error = null
        passwordInputLayout?.error = null
        passwordAgainInputLayout?.error = null
    }

    private fun validateInputs(): Boolean {
        val name = editNameRegister?.text.toString().trim()
        val email = editEmailRegister?.text.toString().trim()
        val password = editPasswordRegister?.text.toString()
        val passwordAgain = editPasswordAgainRegister?.text.toString()

        var isValid = true

        // Validate name
        if (TextUtils.isEmpty(name)) {
            nameInputLayout?.error = "Vui lòng nhập họ tên"
            isValid = false
        } else if (name.length < 2) {
            nameInputLayout?.error = "Họ tên phải có ít nhất 2 ký tự"
            isValid = false
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout?.error = "Vui lòng nhập email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout?.error = "Email không hợp lệ"
            isValid = false
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout?.error = "Vui lòng nhập mật khẩu"
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout?.error = "Mật khẩu phải có ít nhất 6 ký tự"
            isValid = false
        } else if (!isPasswordStrong(password)) {
            passwordInputLayout?.error = "Mật khẩu phải có chữ và số"
            isValid = false
        }

        // Validate password confirmation
        if (TextUtils.isEmpty(passwordAgain)) {
            passwordAgainInputLayout?.error = "Vui lòng xác nhận mật khẩu"
            isValid = false
        } else if (password != passwordAgain) {
            passwordAgainInputLayout?.error = "Mật khẩu không khớp"
            isValid = false
        }

        return isValid
    }

    private fun isPasswordStrong(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }

    private fun handleRegister() {
        val name = editNameRegister?.text.toString().trim()
        val email = editEmailRegister?.text.toString().trim()
        val password = editPasswordRegister?.text.toString()

        progressBar?.visibility = View.VISIBLE
        registerBtn?.isEnabled = false

        mAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth?.currentUser
                    if (user != null) {
                        // Update profile
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Save to Firestore
                                    saveUserToFirestore(user.uid, name, email)
                                } else {
                                    progressBar?.visibility = View.GONE
                                    registerBtn?.isEnabled = true
                                    Toast.makeText(this, "Lỗi cập nhật profile!", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    progressBar?.visibility = View.GONE
                    registerBtn?.isEnabled = true
                    val errorMessage = when {
                        task.exception?.message?.contains("already in use") == true -> 
                            "Email đã được sử dụng"
                        task.exception?.message?.contains("network") == true -> 
                            "Lỗi kết nối mạng"
                        else -> "Đăng ký thất bại: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "role" to role,
            "createdAt" to System.currentTimeMillis(),
            "emailVerified" to false
        )
        
        db?.collection("users")?.document(uid)?.set(userData)
            ?.addOnSuccessListener {
                // Send email verification
                sendEmailVerification()
            }
            ?.addOnFailureListener { e ->
                progressBar?.visibility = View.GONE
                registerBtn?.isEnabled = true
                Toast.makeText(this, "Lỗi lưu dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendEmailVerification() {
        val user = mAuth?.currentUser
        
        progressBar?.visibility = View.GONE
        registerBtn?.isEnabled = true
        
        // Gửi email xác thực (không bắt buộc)
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Đăng ký thành công! Email xác thực đã được gửi.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Đăng ký thành công!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                // Chuyển thẳng vào MainActivity (không cần xác thực)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }

    private fun showVerificationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Xác thực email")
            .setMessage("Chúng tôi đã gửi email xác thực đến ${editEmailRegister?.text}.\n\n" +
                    "Vui lòng kiểm tra hộp thư và nhấn vào link xác thực để hoàn tất đăng ký.\n\n" +
                    "Sau khi xác thực, bạn có thể đăng nhập vào ứng dụng.")
            .setPositiveButton("Đã hiểu") { dialog, _ ->
                dialog.dismiss()
                // Sign out và quay về login
                mAuth?.signOut()
                startActivity(Intent(this, Login::class.java))
                finish()
            }
            .setNegativeButton("Gửi lại") { dialog, _ ->
                dialog.dismiss()
                resendVerificationEmail()
            }
            .setCancelable(false)
            .show()
    }

    private fun resendVerificationEmail() {
        progressBar?.visibility = View.VISIBLE
        mAuth?.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                progressBar?.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đã gửi lại email xác thực!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Lỗi gửi email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
