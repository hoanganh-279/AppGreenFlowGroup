package com.example.appgreenflow.ui.support;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appgreenflow.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SupportFragment extends Fragment {
    private Spinner spinnerIssue;
    private TextInputEditText etDesc;
    private Button btnSubmit;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support, container, false);
        db = FirebaseFirestore.getInstance();
        spinnerIssue = view.findViewById(R.id.spinnerIssue);
        etDesc = view.findViewById(R.id.etDesc);
        btnSubmit = view.findViewById(R.id.btnSubmitSupport);

        setupSpinner();
        btnSubmit.setOnClickListener(v -> submitReport());
        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.support_issues, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIssue.setAdapter(adapter);
    }

    private void submitReport() {
        String issue = spinnerIssue.getSelectedItem().toString();
        String desc = etDesc.getText().toString().trim();
        if (desc.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng mô tả vấn đề!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> report = new HashMap<>();
        report.put("issue", issue);
        report.put("desc", desc);
        report.put("userId", FirebaseAuth.getInstance().getUid());
        report.put("timestamp", System.currentTimeMillis());

        db.collection("support_reports").add(report)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Báo cáo đã gửi thành công!", Toast.LENGTH_SHORT).show();
                    etDesc.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}