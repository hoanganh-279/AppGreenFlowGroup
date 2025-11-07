package com.example.appgreenflow

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {
    private var editNameRegister: TextInputEditText? = null
    private var editEmailRegister: TextInputEditText? = null
    private var editPasswordRegister: TextInputEditText? = null
    private var editPasswordAgainRegister: TextInputEditText? = null
    private var registerBtn: Button? = null
    private var progressBar: ProgressBar? = null
    private var loginNow: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private var role: String? = "customer"

    public override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.getCurrentUser()
        if (currentUser != null) {
            val intent = Intent(getApplicationContext(), MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        role = getIntent().getStringExtra("role")
        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        editNameRegister = findViewById<TextInputEditText>(R.id.editNameRegister)
        editEmailRegister = findViewById<TextInputEditText>(R.id.editEmailRegister)
        editPasswordRegister = findViewById<TextInputEditText>(R.id.editPasswordRegister)
        editPasswordAgainRegister = findViewById<TextInputEditText>(R.id.editPasswordAgainRegister)
        registerBtn = findViewById<Button>(R.id.registerBtn)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        loginNow = findViewById<TextView>(R.id.loginNow)
    }

    private fun setupClickListeners() {
        registerBtn!!.setOnClickListener(View.OnClickListener { v: View? -> handleRegister() })
        loginNow!!.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(Intent(this, Login::class.java))
            finish()
        })
    }

    private fun handleRegister() {
        val name = editNameRegister!!.getText().toString().trim { it <= ' ' }
        val email = editEmailRegister!!.getText().toString().trim { it <= ' ' }
        val password = editPasswordRegister!!.getText().toString().trim { it <= ' ' }
        val passwordAgain = editPasswordAgainRegister!!.getText().toString().trim { it <= ' ' }

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(
                passwordAgain
            )
        ) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != passwordAgain) {
            Toast.makeText(this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Mật khẩu phải ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar!!.setVisibility(View.VISIBLE)
        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(OnCompleteListener { task: Task<AuthResult?>? ->
                progressBar!!.setVisibility(View.GONE)
                if (task!!.isSuccessful()) {
                    val user = mAuth!!.getCurrentUser()
                    if (user != null) {
                        // Lưu profile name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener(OnCompleteListener { updateTask: Task<Void?>? ->
                                if (updateTask!!.isSuccessful()) {
                                    // Lưu role vào Firestore
                                    val userData = hashMapOf(
                                        "name" to name,
                                        "email" to email,
                                        "role" to role,
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                    db!!.collection("users").document(user.getUid()).set(userData)
                                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT)
                                        .show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Cập nhật profile thất bại!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Đăng ký thất bại: " + task.getException()!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}