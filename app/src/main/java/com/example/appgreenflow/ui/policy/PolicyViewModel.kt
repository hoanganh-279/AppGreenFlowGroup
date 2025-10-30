package com.example.appgreenflow.ui.policy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class PolicyViewModel : ViewModel() {
    private val policyText = MutableLiveData<String?>()
    private val db: FirebaseFirestore

    init {
        db = FirebaseFirestore.getInstance()
    }

    fun loadPolicy(role: String) {
        policyText.setValue(null)

        db.collection("policies").document(role)
            .get()
            .addOnSuccessListener(OnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                if (documentSnapshot!!.exists()) {
                    val text = documentSnapshot.getString("text")
                    policyText.setValue(if (text != null) text else "Không tìm thấy chính sách cho " + role + ". Vui lòng liên hệ admin.")
                } else {
                    policyText.setValue("Chính sách chưa được thiết lập cho " + role + ".")
                }
            })
            .addOnFailureListener(OnFailureListener { e: Exception? ->
                policyText.setValue("Lỗi tải chính sách: " + e!!.message)
            })
    }

    fun getPolicyText(): LiveData<String?> {
        return policyText
    }

    fun updatePolicyText(newText: String?) {
        policyText.setValue(newText)
    }
}