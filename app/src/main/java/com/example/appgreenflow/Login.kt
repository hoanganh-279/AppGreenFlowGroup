package com.example.appgreenflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {
    private var editEmailLogin: TextInputEditText? = null
    private var editPasswordLogin: TextInputEditText? = null
    private var loginBtn: Button? = null
    private var spinnerRole: Spinner? = null
    private var progressBar: ProgressBar? = null
    private var registerNow: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null
    private var selectedRole = "customer" // Default

    public override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
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
    }

    private fun initViews() {
        editEmailLogin = findViewById(R.id.editEmailLogin)
        editPasswordLogin = findViewById(R.id.editPasswordLogin)
        loginBtn = findViewById(R.id.loginBtn)
        spinnerRole = findViewById(R.id.spinnerRole)
        progressBar = findViewById(R.id.progressBar)
        registerNow = findViewById(R.id.registerNow)
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
                        Toast.makeText(this@Login, "Vai trò không hợp lệ! Mặc định khách hàng.", Toast.LENGTH_SHORT).show()
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
        loginBtn?.setOnClickListener { handleLogin() }
        registerNow?.setOnClickListener {
            if (selectedRole == "employee") {
                Toast.makeText(this, "Tài khoản nhân viên do admin tạo!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, Register::class.java)
            intent.putExtra("role", selectedRole)
            startActivity(intent)
            finish()
        }
    }

    private fun handleLogin() {
        val email = editEmailLogin?.text.toString().trim()
        val password = editPasswordLogin?.text.toString().trim()
        progressBar?.visibility = View.VISIBLE
        mAuth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                progressBar?.visibility = View.GONE
                if (task.isSuccessful) {
                    val user = mAuth?.currentUser
                    if (user != null && !user.isEmailVerified) {
                        user.sendEmailVerification()
                            .addOnCompleteListener { vTask ->
                                if (vTask.isSuccessful) Toast.makeText(
                                    this,
                                    "Vui lòng xác thực email!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        mAuth?.signOut()
                        return@addOnCompleteListener
                    }
                    db?.collection("users")?.document(user!!.uid)?.get()
                        ?.addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val storedRole = doc.getString("role")
                                if (selectedRole != storedRole) {
                                    Toast.makeText(
                                        this,
                                        "Role không khớp! Liên hệ admin.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    mAuth?.signOut()
                                    return@addOnSuccessListener
                                }
                                // Proceed to MainActivity
                                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT)
                                    .show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "User không tồn tại!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }?.addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Lỗi validate: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(this, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}