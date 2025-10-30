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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
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
        val currentUser = mAuth!!.getCurrentUser()
        if (currentUser != null) {
            val intent = Intent(getApplicationContext(), MainActivity::class.java)
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
        editEmailLogin = findViewById<TextInputEditText>(R.id.editEmailLogin)
        editPasswordLogin = findViewById<TextInputEditText>(R.id.editPasswordLogin)
        loginBtn = findViewById<Button>(R.id.loginBtn)
        spinnerRole = findViewById<Spinner>(R.id.spinnerRole)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        registerNow = findViewById<TextView>(R.id.registerNow)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.user_roles,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole!!.setAdapter(adapter)
        spinnerRole!!.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedRole = if (position == 0) "customer" else "employee"
                // Disable register cho employee
                registerNow!!.setEnabled(position == 0)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    private fun setupClickListeners() {
        loginBtn!!.setOnClickListener(View.OnClickListener { v: View? -> handleLogin() })
        registerNow!!.setOnClickListener(View.OnClickListener { v: View? ->
            if (selectedRole == "employee") {
                Toast.makeText(this, "Tài khoản nhân viên do admin tạo!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, Register::class.java)
            intent.putExtra("role", selectedRole)
            startActivity(intent)
            finish()
        })
    }

    private fun handleLogin() {
        val email = editEmailLogin!!.getText().toString().trim { it <= ' ' }
        val password = editPasswordLogin!!.getText().toString().trim { it <= ' ' }
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(OnCompleteListener { task: Task<AuthResult?>? ->
                progressBar!!.setVisibility(View.GONE)
                if (task!!.isSuccessful()) {
                    val user = mAuth!!.getCurrentUser()
                    if (user != null && !user.isEmailVerified()) {
                        user.sendEmailVerification()
                            .addOnCompleteListener(OnCompleteListener { vTask: Task<Void?>? ->
                                if (vTask!!.isSuccessful()) Toast.makeText(
                                    this,
                                    "Vui lòng xác thực email!",
                                    Toast.LENGTH_LONG
                                ).show()
                            })
                        mAuth!!.signOut()
                        return@addOnCompleteListener
                    }
                    db!!.collection("users").document(user!!.getUid()).get()
                        .addOnSuccessListener(OnSuccessListener { doc: DocumentSnapshot? ->
                            if (doc!!.exists()) {
                                val storedRole = doc.getString("role")
                                if (selectedRole != storedRole) {
                                    Toast.makeText(
                                        this,
                                        "Role không khớp! Liên hệ admin.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    mAuth!!.signOut()
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
                        }).addOnFailureListener(OnFailureListener { e: Exception? ->
                            Toast.makeText(
                                this,
                                "Lỗi validate: " + e!!.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                } else {
                }
            })
    }
}
