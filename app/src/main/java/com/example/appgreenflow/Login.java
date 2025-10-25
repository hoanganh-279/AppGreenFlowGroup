package com.example.appgreenflow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    private TextInputEditText editEmailLogin, editPasswordLogin;
    private Button loginBtn;
    private Spinner spinnerRole;
    private ProgressBar progressBar;
    private TextView registerNow;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedRole = "customer";  // Default

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
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initViews();
        setupSpinner();
        setupClickListeners();
    }

    private void initViews() {
        editEmailLogin = findViewById(R.id.editEmailLogin);
        editPasswordLogin = findViewById(R.id.editPasswordLogin);
        loginBtn = findViewById(R.id.loginBtn);
        spinnerRole = findViewById(R.id.spinnerRole);
        progressBar = findViewById(R.id.progressBar);
        registerNow = findViewById(R.id.registerNow);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = position == 0 ? "customer" : "employee";
                // Disable register cho employee
                registerNow.setEnabled(position == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        loginBtn.setOnClickListener(v -> handleLogin());
        registerNow.setOnClickListener(v -> {
            if (selectedRole.equals("employee")) {
                Toast.makeText(this, "Tài khoản nhân viên do admin tạo!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, Register.class);
            intent.putExtra("role", selectedRole);
            startActivity(intent);
            finish();
        });
    }

    private void handleLogin() {
        String email = editEmailLogin.getText().toString().trim();
        String password = editPasswordLogin.getText().toString().trim();
        // Existing validation...
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && !user.isEmailVerified()) {
                    user.sendEmailVerification().addOnCompleteListener(vTask -> {
                        if (vTask.isSuccessful()) Toast.makeText(this, "Vui lòng xác thực email!", Toast.LENGTH_LONG).show();
                    });
                    mAuth.signOut();  // Force re-login after verify
                    return;
                }
                // Validate role server-side: Query Firestore FIRST
                db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String storedRole = doc.getString("role");
                        if (!selectedRole.equals(storedRole)) {
                            Toast.makeText(this, "Role không khớp! Liên hệ admin.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            return;
                        }
                        // Proceed to MainActivity
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "User không tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi validate: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // Existing error...
            }
        });
    }
}