package com.example.appgreenflow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private TextInputEditText editNameRegister, editEmailRegister, editPasswordRegister, editPasswordAgainRegister;
    private Button registerBtn;
    private ProgressBar progressBar;
    private TextView loginNow;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String role = "customer";

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        role = getIntent().getStringExtra("role");
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        editNameRegister = findViewById(R.id.editNameRegister);
        editEmailRegister = findViewById(R.id.editEmailRegister);
        editPasswordRegister = findViewById(R.id.editPasswordRegister);
        editPasswordAgainRegister = findViewById(R.id.editPasswordAgainRegister);
        registerBtn = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBar);
        loginNow = findViewById(R.id.loginNow);
    }

    private void setupClickListeners() {
        registerBtn.setOnClickListener(v -> handleRegister());
        loginNow.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }

    private void handleRegister() {
        String name = editNameRegister.getText().toString().trim();
        String email = editEmailRegister.getText().toString().trim();
        String password = editPasswordRegister.getText().toString().trim();
        String passwordAgain = editPasswordAgainRegister.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordAgain)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(passwordAgain)) {
            Toast.makeText(this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Lưu profile name
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            // Lưu role vào Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("email", email);
                            userData.put("role", role);
                            userData.put("createdAt", System.currentTimeMillis());
                            db.collection("users").document(user.getUid()).set(userData);
                            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Cập nhật profile thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}