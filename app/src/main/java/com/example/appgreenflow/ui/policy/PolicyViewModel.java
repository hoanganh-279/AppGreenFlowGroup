package com.example.appgreenflow.ui.policy;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class PolicyViewModel extends ViewModel {
    private final MutableLiveData<String> policyText = new MutableLiveData<>();
    private FirebaseFirestore db;

    public PolicyViewModel() {
        db = FirebaseFirestore.getInstance();
    }

    // Method để load policy theo role từ Firestore
    public void loadPolicy(String role) {
        policyText.setValue(null);  // Clear cũ

        db.collection("policies").document(role)  // Doc: "customer" hoặc "employee"
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String text = documentSnapshot.getString("text");
                        policyText.setValue(text != null ? text : "Không tìm thấy chính sách cho " + role + ". Vui lòng liên hệ admin.");
                    } else {
                        policyText.setValue("Chính sách chưa được thiết lập cho " + role + ".");
                    }
                })
                .addOnFailureListener(e -> {
                    policyText.setValue("Lỗi tải chính sách: " + e.getMessage());
                });
    }

    public LiveData<String> getPolicyText() {
        return policyText;
    }

    public void updatePolicyText(String newText) {
        policyText.setValue(newText);
    }
}